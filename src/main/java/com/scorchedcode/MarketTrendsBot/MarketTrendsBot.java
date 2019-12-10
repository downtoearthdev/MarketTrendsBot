package com.scorchedcode.MarketTrendsBot;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.Timer;
import java.util.TimerTask;

public class MarketTrendsBot {
    private static MarketTrendsBot instance;
    private String TOKEN;
    //private String channelName;
    //private TextChannel channel;
    private JDA api;

    private MarketTrendsBot() {

    }

    public static void main(String[] args) {
        MarketTrendsBot bot = MarketTrendsBot.getInstance();
        bot.initDiscordBot();
        bot.loadFeeds();
    }

    public void initDiscordBot() {
        handleConfig();
        try {
            api = new JDABuilder(AccountType.BOT).setToken(TOKEN).build().awaitReady();
        } catch (LoginException | IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //channel = api.getTextChannelsByName(channelName, true).get(0);
        api.addEventListener(new DiscordListener());
    }

    private void handleConfig() {
        if (!new File("config.json").exists()) {
            try {
                InputStream is = MarketTrendsBot.class.getResourceAsStream("/config.json");
                File config = new File("config.json");
                FileWriter os = new FileWriter(config);
                while (is.available() > 0)
                    os.write(is.read());
                is.close();
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            String contents = new String(Files.readAllBytes(new File("config.json").toPath()));
            JSONObject obj = new JSONObject(contents);
            TOKEN = obj.getString("token");
            //channelName = obj.getString("channel");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (TOKEN == null || TOKEN.isEmpty())// || channelName == null || channelName.isEmpty())
            System.exit(0);
    }

    private void loadFeeds() {
        File savedFeeds = new File("feeds.json");
        if(savedFeeds.exists()) {
            try {
                JSONObject obj = new JSONObject(String.join("", Files.readAllLines(savedFeeds.toPath())));
                for(String feed : obj.keySet()) {
                    if(!feed.equals("empty"))
                        new Timer().scheduleAtFixedRate(new FeedRetrieverTask(Feeds.valueOf(feed), obj.getString(feed)), 1000L, 600000L);
                }
            } catch (IOException e) {

            }
        }
    }


    public static MarketTrendsBot getInstance() {
        if (instance == null)
            instance = new MarketTrendsBot();
        return instance;
    }

    public JDA getApi() {
        return api;
    }
    //public TextChannel getChannel() {
    //    return channel;
    //}

}
