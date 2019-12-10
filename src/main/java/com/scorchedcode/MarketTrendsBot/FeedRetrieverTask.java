package com.scorchedcode.MarketTrendsBot;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.TimerTask;

public class FeedRetrieverTask extends TimerTask
{
    private TextChannel output;
    private Feeds rss;
    private int interval;
    private static ArrayList<Feeds> instances = new ArrayList<>();

    public FeedRetrieverTask(Feeds rss, String outputName) {
        this.rss = rss;
        this.output = MarketTrendsBot.getInstance().getApi().getTextChannelById(outputName);
        if(instances.contains(this.rss))
            cancel();
        instances.add(this.rss);
    }

    @Override
    public void run() {
        String newNewsLink = "";
        if (rss.isRSS()) {
            try {
                SyndFeed feed = new SyndFeedInput().build(new XmlReader(new URL(rss.getURL())));
                if(rss == Feeds.FXSTREET)
                    System.out.println(feed.getEntries().get(0).getLink());
                newNewsLink = feed.getEntries().get(0).getLink();
            } catch (FeedException e) {

            } catch (IOException e) {

            }
        } else {
            String selector = "";
            switch (rss) {
                case MARKETWATCH_LATEST_NEWS:
                    selector = "body > div.container.container--dynamic.zone--1 > div.region.region--primary > div.component.component--module.more-headlines > div:nth-child(2) > div.group.group--headlines > div:nth-child(1) > div > h3 > a";
                    break;
                case MARKETWATCH_HP_SPOTLIGHT:
                    selector = "body > div.container.container--dynamic.zone--1 > div.region.region--primary > div.component.component--layout.layout-2 > div.column.column--primary > div:nth-child(1) > div > h3 > a";
                    break;
                case COINDESK:
                    selector = "#article-streams > div > div.article-set > a:nth-child(1)";
                    break;
            }
            String userAgent = "Mozilla/5.0 (jsoup)";
            int timeout = 5 * 1000;
            Document doc = null;
            try {
                doc = Jsoup.connect(rss.getURL()).userAgent(userAgent).timeout(timeout).get();
                newNewsLink = doc.select(selector).get(0).attr("href");
            } catch (Exception e) {

            }
        }
        Webhook hook = null;
        for (Webhook web : ((TextChannel) output).retrieveWebhooks().complete()) {
            if (web.getName().equals("MarketTrendsBot"))
                hook = web;
        }
        if (hook == null)
            hook = ((TextChannel) output).createWebhook("MarketTrendsBot").complete();
        for (Message msg : output.getIterableHistory().complete()) {
            if (msg.isWebhookMessage() && msg.getAuthor().getName().equals(rss.getBotName())) {
                String oldNewsLink = msg.getContentRaw();
                if (!oldNewsLink.equalsIgnoreCase(newNewsLink)) {
                    WebhookClient client = new WebhookClientBuilder(hook.getUrl()).build();
                    WebhookMessageBuilder wmb = new WebhookMessageBuilder();
                    wmb.setUsername(rss.getBotName());
                    wmb.setContent(newNewsLink);
                    wmb.setAvatarUrl(rss.getFavicon());
                    try{
                        client.send(wmb.build());
                    }
                    catch (Exception e) {

                    }
                }
                return;
            }
        }
        WebhookClient client = new WebhookClientBuilder(hook.getUrl()).build();
        WebhookMessageBuilder wmb = new WebhookMessageBuilder();
        wmb.setUsername(rss.getBotName());
        wmb.setContent(newNewsLink);
        wmb.setAvatarUrl(rss.getFavicon());
        try {
            client.send(wmb.build());
        }
        catch (Exception e) {

        }
    }
}
