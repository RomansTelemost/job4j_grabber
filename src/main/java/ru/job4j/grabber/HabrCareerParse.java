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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    private static final Map<String, String> LINK_DESCRIPTION_MAP = new HashMap<>();

    private static final Logger LOG = LoggerFactory.getLogger(HabrCareerParse.class.getName());

    public static void main(String[] args) throws IOException {
        List<String> links = new ArrayList<>();
        List<Integer> pageNumbers = List.of(1, 2, 3, 4, 5);
        for (Integer pageNumber : pageNumbers) {
            String numberedPageLink = PAGE_LINK + "?page=" + pageNumber;
            Connection connection = Jsoup.connect(numberedPageLink);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element titleElement2 = row.select(".vacancy-card__date").first();

                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                String date = titleElement2.select(".basic-date").first().attr("datetime");

                HabrCareerDateTimeParser habrCareerDateTimeParser = new HabrCareerDateTimeParser();
                LocalDateTime localDateTime = habrCareerDateTimeParser.parse(date);

                String link = String.format("%s%s %s", SOURCE_LINK, linkElement.attr("href"), localDateTime.toLocalDate());
                System.out.printf("%s %s%n", vacancyName, link);
                System.out.println("-*-");

                links.add(String.format("%s%s", SOURCE_LINK, linkElement.attr("href")));
            });
        }
        links.stream().forEach(link -> LINK_DESCRIPTION_MAP.put(link, retrieveDescription(link)));
        System.out.println(LINK_DESCRIPTION_MAP.get("https://career.habr.com/vacancies/1000122487"));
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
