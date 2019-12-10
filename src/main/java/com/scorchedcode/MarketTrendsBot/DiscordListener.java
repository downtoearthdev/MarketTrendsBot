package com.scorchedcode.MarketTrendsBot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.utils.IOUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Timer;

public class DiscordListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if((event.getChannel().getId().equals("621364528701243402") || event.getChannel().getId().equals("600356580831133727")) && event.getMessage().getContentRaw().indexOf("$") == 0) {
            String acronym = event.getMessage().getContentRaw().split(" ")[0].replaceAll("\\$", "");
            String advChartSelector = "#chartcontent > div > div.customchart.acenter.bedonkbottom > table > tbody > tr > td.padded.vatop > img";
            String userAgent = "Mozilla/5.0 (jsoup)";
            //boolean advChart = (event.getMessage().getContentRaw().split(" ").length == 2 && event.getMessage().getContentRaw().split(" ")[1].equalsIgnoreCase("c4")) ? true : false;
            int timeout = 5 * 1000;
            Document doc = null;
            try {
                doc = Jsoup.connect("http://bigcharts.marketwatch.com/advchart/frames/frames.asp?show=&insttype=Stock&symb="+acronym.toUpperCase()+"&time=8&freq=1&compidx=aaaaa%3A0&comptemptext=&comp=none&ma=2&maval=9&uf=0&lf=2&lf2=1&lf3=4&type=2&style=320&size=2&x=67&y=17&timeFrameToggle=false&compareToToggle=false&indicatorsToggle=false&chartStyleToggle=false&state=10").userAgent(userAgent).timeout(timeout).get();
                Element img = doc.body().selectFirst(advChartSelector);
                String link = img.attr("src");
                event.getChannel().sendFile(new URL(link).openStream(), "chart.png").content("Daily chart for " + doc.selectFirst("#quote > tbody > tr.header > td:nth-child(1) > div:nth-child(2)").text()).queue();
            }
            catch (Exception e) {
                event.getChannel().sendMessage("No such symbol found.").queue();
            }

        }
        if(event.getMessage().getContentRaw().equals("!marketbot")) {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("MarketTrendsBot Help")
                    .addField("!add <feed>", "Add a feed to a channel.\nPossible feed types are: cnbc, fxstreet, marketwatchtop, marketwatchspotlight, coindesk, seekingalpha", false)
                    .addField("$<market symbol>", "Check an advanced graph of the market trend for this symbol (ie. $aapl for Apple's trend).", false);
            event.getChannel().sendMessage(eb.build()).queue();
        }

        if(event.getMessage().getContentRaw().split(" ")[0].equalsIgnoreCase("!add") && event.getMember().isOwner()) {
            String[] args = event.getMessage().getContentRaw().split(" ");
            if(args.length == 2) {
                switch (args[1]) {
                    case "cnbc":
                        new Timer().scheduleAtFixedRate(new FeedRetrieverTask(Feeds.CNBC, event.getChannel().getId()), 1000L, 600000L);
                        serializeOption(event.getChannel().getId(), Feeds.CNBC);
                        break;
                    case "seekingalpha":
                        new Timer().scheduleAtFixedRate(new FeedRetrieverTask(Feeds.SEEKINGALPHA, event.getChannel().getId()), 1000L, 600000L);
                        serializeOption(event.getChannel().getId(), Feeds.SEEKINGALPHA);
                        break;
                    case "marketwatchtop":
                        new Timer().scheduleAtFixedRate(new FeedRetrieverTask(Feeds.MARKETWATCH_LATEST_NEWS, event.getChannel().getId()), 1000L, 600000L);
                        serializeOption(event.getChannel().getId(), Feeds.MARKETWATCH_LATEST_NEWS);
                        break;
                    case "marketwatchspotlight":
                        new Timer().scheduleAtFixedRate(new FeedRetrieverTask(Feeds.MARKETWATCH_HP_SPOTLIGHT, event.getChannel().getId()), 1000L, 600000L);
                        serializeOption(event.getChannel().getId(), Feeds.MARKETWATCH_HP_SPOTLIGHT);
                        break;
                    case "fxstreet":
                        new Timer().scheduleAtFixedRate(new FeedRetrieverTask(Feeds.FXSTREET, event.getChannel().getId()), 1000L, 600000L);
                        serializeOption(event.getChannel().getId(), Feeds.FXSTREET);
                        break;
                    case "coindesk":
                        new Timer().scheduleAtFixedRate(new FeedRetrieverTask(Feeds.COINDESK, event.getChannel().getId()), 1000L, 600000L);
                        serializeOption(event.getChannel().getId(), Feeds.COINDESK);
                        break;
                }
            }
        }
    }

    private void serializeOption(String id, Feeds feed) {
        File savedFeeds = new File("feeds.json");
        JSONObject obj = null;
        try {
            FileUtils.touch(savedFeeds);
            obj = new JSONObject(String.join("", Files.readAllLines(savedFeeds.toPath())));

        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (JSONException e) {
            obj = new JSONObject();
        }
        obj.put(feed.toString(), id);
        try {
            Files.write(savedFeeds.toPath(), obj.toString().getBytes(), StandardOpenOption.WRITE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
