package roppy.dq10.rankanalytics.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import roppy.dq10.rankanalytics.scraper.dto.RankItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BattleTrinityScraper implements Scraper {
    private static final int PAGE_NUMBER = 4;
    private static final int [] URL_SUBRACE_MAPPER = {6,1,2,3,4,5};

    @Override
    public List<List<RankItem>> scrape(int round, int subraceNumber) throws IOException, ScrapingException {
        if(subraceNumber != 6){
            throw new ScrapingException("Illegal subrace number.");
        }

        List<List<RankItem>> listList = new ArrayList<>();

        for(int subraceIndex = 0; subraceIndex < URL_SUBRACE_MAPPER.length; subraceIndex++) {
            List<RankItem> list = new ArrayList<>();
            listList.add(list);

            for(int page = 0; page < PAGE_NUMBER;page++) {

                String url = String.format("https://hiroba.dqx.jp/sc/worldRanking/battleTrinity/%d/%d/page/%d",round, URL_SUBRACE_MAPPER[subraceIndex],page);

                Document document = Jsoup.connect(url).get();
                Elements rankers = document.select("tr.firstrank,tr.secondrank,tr.thirdrank,tr.rank");
                for (Element ranker : rankers) {
                    RankItem r = new RankItem();
                    int rank;
                    try{
                        rank = Integer.parseInt(ranker.select(".col1 .rank").get(0).html().replace("位", "").trim());
                    }catch (NumberFormatException nfe){
                        throw new ScrapingException(nfe);
                    }
                    r.setRank(rank);
                    r.setName(ranker.select(".col2 p").get(0).html().trim());
                    r.setExtraData("");
                    int point;
                    try{
                        point = Integer.parseInt(ranker.select(".col3 .pointget-col34").get(0).html().replace("&nbsp;点", "").replace(",", "").trim());
                    }catch (NumberFormatException nfe){
                        throw new ScrapingException(nfe);
                    }
                    r.setPoint(point);

                    list.add(r);
                }
            }
        }
        return listList;
    }
}
