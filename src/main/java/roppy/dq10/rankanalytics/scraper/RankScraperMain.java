package roppy.dq10.rankanalytics.scraper;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import roppy.dq10.rankanalytics.scraper.dto.RankItem;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RankScraperMain implements RequestHandler<HandlerInput,Object> {


    private static final Map<String,RaceConfig> RACE_CONFIG_MAP;
    static {
        Map<String,RaceConfig> m = new HashMap<>();
        for(RaceConfig r : RaceConfig.values()){
            m.put(r.getKey(),r);
        }
        RACE_CONFIG_MAP = Collections.unmodifiableMap(m);
    }

    // 第一引数はScraper設定 未指定の場合は 環境変数 RANK_SCRAPER 値を取得
    // 第二引数は回次 未指定の場合は 環境変数 RANK_ROUND 値を取得

    public static void main(String args[]) throws Exception {
        String scraperKey;
        if (args.length == 0) {
            scraperKey = System.getenv("RANK_SCRAPER");
        } else {
            scraperKey = args[0];
        }

        if (!RACE_CONFIG_MAP.containsKey(scraperKey)) {
            System.out.println("scraper key list");
            for (RaceConfig r : RaceConfig.values()) {
                System.out.println(r.getKey());
            }
            throw new InitializationException("Unknown scraperkey:" + scraperKey);
        }

        int round;
        try {
            if (args.length <= 1) {
                round = Integer.parseInt(System.getenv("RANK_ROUND"));
            } else {
                round = Integer.parseInt(args[1]);
            }
        } catch (NumberFormatException nfe) {
            throw new InitializationException("Round should be positive integer", nfe);
        }

        HandlerInput input = new HandlerInput();
        input.setScraperKey(scraperKey);
        input.setRound(round);

        RankScraperMain mainObject = new RankScraperMain();
        mainObject.handleRequest(input,null);

    }

    @Override
    public Object handleRequest(HandlerInput input, Context context) {
        try {
            int round = input.getRound();
            RaceConfig raceConfig = RACE_CONFIG_MAP.get(input.getScraperKey());
            if(raceConfig == null) {
                throw new RuntimeException("Unknown scraperkey:" + input.getScraperKey());
            }

            Scraper scraper = (Scraper) Class.forName(raceConfig.getClassName()).newInstance();

            List<List<RankItem>> list = scraper.scrape(round, raceConfig.getSubraceNumber());

            S3Uploader uploader = S3Uploader.getInstance();
            if (uploader.compareLatest(list, raceConfig.getStringObjKeyPrefix(), round)) {
                throw new ScrapingException("Scraped data is the latest one.");
            }

            uploader.upload(list, raceConfig.getStringObjKeyPrefix(), round, raceConfig.getUpdateTimeConfig());
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        return new Object();
    }
}
