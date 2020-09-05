package roppy.dq10.rankanalytics.scraper;

public class ScrapingException extends Exception {
    public ScrapingException(){
        super();
    }
    public ScrapingException(Throwable t){
        super(t);
    }
    public ScrapingException(String s){
        super(s);
    }
    public ScrapingException(String s,Throwable t){
        super(s,t);
    }
}
