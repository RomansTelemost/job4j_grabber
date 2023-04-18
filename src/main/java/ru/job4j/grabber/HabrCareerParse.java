package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    public static void main(String[] args) throws IOException {
        Connection connection = Jsoup.connect(PAGE_LINK);
        Document document = connection.get();
        Elements rows = document.select(".vacancy-card__inner");
        rows.forEach(row -> {
            Element titleElement = row.select(".vacancy-card__title").first();
            Element titleElement2 = row.select(".vacancy-card__date").first();

            Element linkElement = titleElement.child(0);
            String vacancyName = titleElement.text();
            String date = titleElement2.select(".basic-date").first().attr("datetime");
            date = date.substring(0, date.length() - 6);

            HabrCareerDateTimeParser habrCareerDateTimeParser = new HabrCareerDateTimeParser();
            LocalDateTime localDateTime = habrCareerDateTimeParser.parse(date);

            String link = String.format("%s%s %s", SOURCE_LINK, linkElement.attr("href"), localDateTime.toLocalDate());
            System.out.printf("%s %s%n", vacancyName, link);
        });
    }
}