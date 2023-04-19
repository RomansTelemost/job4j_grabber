package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);

    private static final Logger LOG = LoggerFactory.getLogger(HabrCareerParse.class.getName());

    private static final int PAGE_COUNT = 5;

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> list(String link) {
        List<Post> list = new ArrayList<>();
        try {
            for (int i = 0; i < PAGE_COUNT; i++) {
                Connection connection = Jsoup.connect(String.format("%s%s", link, i));
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> {
                    Element titleElement = row.select(".vacancy-card__title").first();
                    Element titleElement2 = row.select(".vacancy-card__date").first();

                    Element linkElement = titleElement.child(0);
                    String vacancyName = titleElement.text();
                    String date = titleElement2.select(".basic-date").first().attr("datetime");
                    LocalDateTime localDateTime = dateTimeParser.parse(date);

                    String description = retrieveDescription(String.format("%s%s", SOURCE_LINK, linkElement.attr("href")));

                    String vacancyLink = String.format("%s%s", SOURCE_LINK,
                            linkElement.attr("href"));
                    Post post = new Post(vacancyName, vacancyLink, description, localDateTime);
                    list.add(post);
                });
            }
        } catch (IOException e) {
            LOG.error("Connection error", e);
        }
        return list;
    }

    public static void main(String[] args) {
        HabrCareerParse habrCareerParse = new HabrCareerParse(new HabrCareerDateTimeParser());
        System.out.println(habrCareerParse.list(PAGE_LINK));
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
