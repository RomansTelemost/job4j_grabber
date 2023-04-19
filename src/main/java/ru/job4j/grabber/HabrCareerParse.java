package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);

    private static final Logger LOG = LoggerFactory.getLogger(HabrCareerParse.class.getName());

    private static final int PAGE_COUNT = 5;

    public static void main(String[] args) throws IOException {
        for (int i = 0; i < PAGE_COUNT; i++) {
            Connection connection = Jsoup.connect(String.format("%s%s", PAGE_LINK, i));
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element titleElement2 = row.select(".vacancy-card__date").first();

                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                String date = titleElement2.select(".basic-date").first().attr("datetime");
                String description = retrieveDescription(String.format("%s%s", SOURCE_LINK, linkElement.attr("href")));

                HabrCareerDateTimeParser habrCareerDateTimeParser = new HabrCareerDateTimeParser();
                LocalDateTime localDateTime = habrCareerDateTimeParser.parse(date);

                String link = String.format("%s%s %s %s", SOURCE_LINK,
                        linkElement.attr("href"),
                        localDateTime.toLocalDate(),
                        description);
                System.out.printf("%s %s%n", vacancyName, link);
            });
        }
    }

    public static String retrieveDescription(String link) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            Connection connection = Jsoup.connect(link);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-description__text");
            rows.forEach(row -> {
                stringBuilder.append(row.text());
                stringBuilder.append(System.lineSeparator());
            });
        } catch (IOException e) {
            LOG.error("Pars description", e);
        }
        return stringBuilder.toString();
    }
}
