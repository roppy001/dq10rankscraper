package roppy.dq10.rankanalytics.scraper;

import roppy.dq10.rankanalytics.scraper.dto.RankItem;

import java.io.IOException;
import java.util.List;

public interface Scraper {
    List<List<RankItem>> scrape(int round,int subraceNumber) throws IOException,ScrapingException;
}
