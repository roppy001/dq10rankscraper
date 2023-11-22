package roppy.dq10.rankanalytics.scraper;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RaceConfig {
    SLIME_RACE("slimerace","roppy.dq10.rankanalytics.scraper.SlimeRaceScraper","slimerace",1,UpdateTimeConfig.EIGHT_HOURS),
    BATTLE_TRINITY("trinity","roppy.dq10.rankanalytics.scraper.BattleTrinityScraper","trinity",6,UpdateTimeConfig.EIGHT_HOURS),
    CASINO_RAID_1("casinoraid1","roppy.dq10.rankanalytics.scraper.CasinoRaidScraper","casinoraid",3,UpdateTimeConfig.EIGHT_HOURS),
    CASINO_RAID_2("casinoraid2","roppy.dq10.rankanalytics.scraper.CasinoRaidScraper","casinoraid",4,UpdateTimeConfig.EIGHT_HOURS),
    BATTLE_PENCIL("pencil","roppy.dq10.rankanalytics.scraper.BattlePencilScraper","pencil",1,UpdateTimeConfig.EIGHT_HOURS),
//    COLOSSEUM("colosseum","roppy.dq10.rankanalytics.scraper.ColosseumScraper","colosseum",1),
    DAIFUGO("daifugo","roppy.dq10.rankanalytics.scraper.DaifugoScraper","daifugo",1,UpdateTimeConfig.ONE_DAY),
    DAIFUGO_MATCH("daifugom","roppy.dq10.rankanalytics.scraper.DaifugoMatchScraper","daifugom",1,UpdateTimeConfig.ONE_DAY),
    DAIFUGO_MATCH2("daifugom2","roppy.dq10.rankanalytics.scraper.DaifugoMatchScraper","daifugom",1,UpdateTimeConfig.EIGHT_HOURS),
    FISHING_CONTEST_1("fishing1","roppy.dq10.rankanalytics.scraper.FishingContestScraper","fishing",1,UpdateTimeConfig.EIGHT_HOURS),
    FISHING_CONTEST_2("fishing2","roppy.dq10.rankanalytics.scraper.FishingContestScraper","fishing",2,UpdateTimeConfig.EIGHT_HOURS);

    private String key;
    private String className;
    private String stringObjKeyPrefix;
    private int subraceNumber;
    UpdateTimeConfig updateTimeConfig;
}
