package tw.asts.mc.asts.util.discordsrv;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.okhttp3.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import tw.asts.mc.asts.util.text;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class DiscordSRVWeb implements Listener {
    private HttpServer server;
    private final Plugin plugin;
    private final FileConfiguration config;
    public DiscordSRVWeb(Plugin plugin, FileConfiguration config) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
        this.config = config;
        startServer();
    }
    @EventHandler
    public void onDisable(PluginDisableEvent event) {
        server.stop(0);
    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerJoin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        String discordId = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(uuid);
        if (discordId != null) {
            return;
        }
        String httpHost = config.getString("discord.host");
        if (httpHost == null) {
            return;
        }
        boolean https = config.getBoolean("discord.https");
        String code = DiscordSRV.getPlugin().getAccountLinkManager().generateCode(uuid);
        String url = "http" + (https ? "s" : "") + "://" + httpHost + "/c/" + code;
        YamlConfiguration discordSRVLinking = YamlConfiguration.loadConfiguration(DiscordSRV.getPlugin().getLinkingFile());
        String message = discordSRVLinking.getString("Require linked account to play.Not linked message");
        if (message == null) {
            message = "&7請連接您的&9Discord&7帳號方可遊玩。\n&7請將&b{CODE}&7傳送到&9Discord&7中的&b{BOT}\n&7或開啟認證連結 »&b {URL}\n&7來連結您的帳號。";
        }
        String botName = config.getString("discord.bot");
        assert botName != null;
        message = message.replaceAll("&", "§").replace("{CODE}", code).replace("{BOT}", botName).replace("{URL}", "<click:open_url:'" + url + "'>" + url + "</click>");
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, text.miniMessageComponent(text.miniMessage(message)));
    }
    public void startServer() {
        try {
            int port = config.getInt("discord.port");
            System.setProperty("sun.net.httpserver.maxReqTime", "10000");
            System.setProperty("sun.net.httpserver.maxRspTime", "10000");
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/cb/", new CallbackLink(config, plugin));
            server.createContext("/c/", new CodeLink(config, plugin));
            server.createContext("/", new AllPath(config));
            server.setExecutor(null);
            server.start();
        }
        catch (IOException e) {
            return;
        }
    }
}
class AllPath implements HttpHandler {
    private final FileConfiguration config;
    public AllPath(FileConfiguration config) {
        this.config = config;
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Location", config.getString("discord.home_page"));
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }
}
class CodeLink implements HttpHandler {
    private final FileConfiguration config;
    private final Plugin plugin;
    public CodeLink(FileConfiguration config, Plugin plugin) {
        this.config = config;
        this.plugin = plugin;
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.length() != 7) {
            exchange.sendResponseHeaders(400, -1);
            exchange.close();
            return;
        }
        String code = path.substring(3, 7).replaceAll("[^0-9]", "");
        if (code.length() != 4 || !DiscordSRV.getPlugin().getAccountLinkManager().getLinkingCodes().containsKey(code)) {
            exchange.sendResponseHeaders(400, -1);
            exchange.close();
            return;
        }
        String httpHost = config.getString("discord.host");
        if (httpHost == null) {
            return;
        }
        boolean https = config.getBoolean("discord.https");
        String redirectUri = "http" + (https ? "s" : "") + "://" + httpHost + "/cb/";
        String url = "https://discord.com/api/oauth2/authorize?client_id=" + config.getString("discord.client_id") + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) + "&response_type=code&scope=identify&state=" + code;
        exchange.getResponseHeaders().set("Location", url);
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }
}
class CallbackLink implements HttpHandler {
    private final FileConfiguration config;
    private final Plugin plugin;
    private static OkHttpClient client = new OkHttpClient();
    public CallbackLink(FileConfiguration config, Plugin plugin) {
        this.config = config;
        this.plugin = plugin;
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) {
            exchange.sendResponseHeaders(400, -1);
            exchange.close();
            return;
        }
        Map<String, String> params = queryToMap(query);
        if (!params.containsKey("code") || !params.containsKey("state")) {
            exchange.sendResponseHeaders(400, -1);
            exchange.close();
            return;
        }
        String code = params.get("state");
        if (!DiscordSRV.getPlugin().getAccountLinkManager().getLinkingCodes().containsKey(code)) {
            exchange.sendResponseHeaders(403, -1);
            exchange.close();
            return;
        }
        String tokenResponse = post("https://discord.com/api/oauth2/token", Map.of("client_id", config.getString("discord.client_id"), "client_secret", config.getString("discord.client_secret"), "grant_type", "authorization_code", "code", params.get("code"), "redirect_uri", ("http" + (config.getBoolean("discord.https") ? "s" : "") + "://" + config.getString("discord.host") + "/cb/")));
        String userResponse = get("https://discord.com/api/users/@me", JsonParser.parseString(tokenResponse).getAsJsonObject().get("access_token").getAsString());
        JsonObject user = JsonParser.parseString(userResponse).getAsJsonObject();
        String discordId = user.get("id").getAsString();
        if (discordId == null) {
            exchange.sendResponseHeaders(403, -1);
            exchange.close();
            return;
        }
        DiscordSRV.getPlugin().getAccountLinkManager().process(code, discordId);
        exchange.getResponseHeaders().set("Location", config.getString("discord.home_page"));
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }
    private static Map<String, String> queryToMap(String query) {
        return query != null ? Arrays.stream(query.split("&")).map(param -> param.split("=")).collect(Collectors.toMap(pair -> URLDecoder.decode(pair[0], StandardCharsets.UTF_8), pair -> URLDecoder.decode(pair[1], StandardCharsets.UTF_8))) : new HashMap<>();
    }
    private static String get(String url, String accessToken) throws IOException {
        Request request = new Request.Builder().url(url).addHeader("Authorization", "Bearer " + accessToken).build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
    private static String post(String url, Map<String, String> params) throws IOException {
        String parameters = params.entrySet().stream().map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)).collect(Collectors.joining("&"));
        RequestBody body = RequestBody.create(MediaType.get("application/x-www-form-urlencoded; charset=UTF-8"), parameters);
        Request request = new Request.Builder().url(url).post(body).build();
        try (Response response = client.newCall(request).execute()) {
            assert response.body() != null;
            return response.body().string();
        }
    }
}