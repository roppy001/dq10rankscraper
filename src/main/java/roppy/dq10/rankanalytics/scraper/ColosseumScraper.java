package roppy.dq10.rankanalytics.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import roppy.dq10.rankanalytics.scraper.dto.RankItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ColosseumScraper implements Scraper {
    private static final int PAGE_NUMBER = 7;
    private static final String REMOVED_CHARACTER_NAME = "－－－";

    @Override
    public List<List<RankItem>> scrape(int round, int subraceNumber) throws IOException,ScrapingException {
        if(subraceNumber != 1){
            throw new ScrapingException("Illegal subrace number.");
        }

        List<List<RankItem>> listList = new ArrayList<>();

        List<RankItem> list = new ArrayList<>();
        listList.add(list);

        for(int page = 0; page < PAGE_NUMBER;page++) {

            String url = String.format("https://hiroba.dqx.jp/sc/worldRanking/colosseumRanking/%d/ranking/page/%d",round,page);

            Document document = Jsoup.connect(url).get();

            System.out.println(document.toString());

            Elements rankers = document.select("tr.firstrank,tr.secondrank,tr.thirdrank,tr.rank");

            System.out.println(url + "," + rankers.size());

            for (Element ranker : rankers) {
                RankItem r = new RankItem();
                int rank;
                try{
                    rank = Integer.parseInt(ranker.select(".col1 .rank").get(0).html().replace("位", "").trim());
                }catch (NumberFormatException nfe){
                    throw new ScrapingException(nfe);
                }
                r.setRank(rank);
                r.setName(ranker.select(".col3 .name").get(0).html().trim());
                if(REMOVED_CHARACTER_NAME.equals(r.getName())){
                    continue;
                }

                String rate = ranker.select(".col4").get(0).html().replace("%", "").replace(".", "").trim();
                String rankCategoryName = ranker.select(".col3 .c_rank").get(0).html().trim();
                String teamName = ranker.select(".col3 .team").get(0).html().trim();

                r.setExtraData(rate + "," + rankCategoryName + "," + teamName);

                int point;
                try{
                    point = Integer.parseInt(ranker.select(".col2").get(0).html().replace("P", "").trim());
                }catch (NumberFormatException nfe){
                    throw new ScrapingException(nfe);
                }
                r.setPoint(point);

                list.add(r);
            }
        }
        return listList;
    }
}
