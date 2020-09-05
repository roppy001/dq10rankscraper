package roppy.dq10.rankanalytics.scraper.dto;

import lombok.Data;

@Data
public class RankItem {
    private int rank;

    private int point;

    private String name;

    private String extraData;
}
