import java.util.concurrent.ForkJoinPool;

public class Main {
    public static void main(String[] args) {
        final String mainLink = "http://www.playback.ru/";
        PageParser parser = new PageParser(mainLink);
        new ForkJoinPool().invoke(new WebPageLinkExtractor(parser));
    }
}
