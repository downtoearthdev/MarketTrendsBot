package com.scorchedcode.MarketTrendsBot;

public enum Feeds {
    CNBC("https://www.cnbc.com/id/100003114/device/rss/rss.html", true, "CNBC Bot", "https://www.cnbc.com/favicon.ico"),
    SEEKINGALPHA("https://seekingalpha.com/news/all/feed", true, "SeekingAlpha Bot", "https://seekingalpha.com/favicon.ico"),
    MARKETWATCH_LATEST_NEWS("https://www.marketwatch.com/latest-news", false, "MarketWatchLatest Bot", "https://www.marketwatch.com/favicon.ico"),
    MARKETWATCH_HP_SPOTLIGHT("https://www.marketwatch.com/markets/earnings?mod=hp_spotlight", false, "MarketWatchSpotlight Bot", "https://www.marketwatch.com/favicon.ico"),
    FXSTREET("http://xml.fxstreet.com/news/forex-news/index.xml", true, "FXStreet Bot", "https://staticcontent.fxstreet.com/website/static-html/favicon.png"),
    COINDESK("https://www.coindesk.com/", false, "CoinDesk Bot", "https://coindesk.com/favicon.ico");

    private String url;
    private boolean isRSS;
    private String botName;
    private String favicon;
    Feeds(String url, boolean isRSS, String botName, String favicon) {
        this.url = url;
        this.isRSS = isRSS;
        this.botName = botName;
        this.favicon = favicon;
    }

    public String getURL() {
        return url;
    }

    public boolean isRSS() {
        return isRSS;
    }

    public String getBotName() {
        return botName;
    }

    public String getFavicon() {
        return favicon;
    }
}
