package hexlet.code.controllers;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.model.query.QUrl;
import hexlet.code.parser.ParserUrl;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Objects;

public final class UrlController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlController.class);
    public static Handler createUrl = ctx -> {
        String input = ctx.formParam("url");
        try {
            if (Objects.equals(input, null) || Objects.equals(input, "")) {
                ctx.sessionAttribute("flashEmpty", "Url не может быть пустым");
                ctx.redirect("/");
            }
            Url url = new Url(ParserUrl.parse(input));
            boolean existUrl = new QUrl().name.equalTo(url.getName()).exists();
            if (existUrl) {
                ctx.sessionAttribute("flashWarning", "страница уже создана");
                ctx.redirect("/urls");
            } else {
                url.save();
                ctx.sessionAttribute("flashSuccess", "Страница успешно добавлена");
                ctx.redirect("/urls");
            }
        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flashError", "некорректный url");
            ctx.redirect("/");
        }
    };

    public static Handler listUrls = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        int rowsPerPage = 10;
        int offset = (page - 1) * rowsPerPage;

        PagedList<Url> pagedUrls = new QUrl()
                .setFirstRow(offset)
                .setMaxRows(rowsPerPage)
                .orderBy()
                .id.asc()
                .findPagedList();

        List<Url> urls = pagedUrls.getList();

        ctx.attribute("urls", urls);
        ctx.attribute("page", page);
        ctx.render("urls/index.html");
    };

    public static Handler showUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);
        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (url == null) {
            throw new NotFoundResponse();
        }
        ctx.attribute("url", url);
        ctx.render("urls/show.html");
    };

    public static Handler checkUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);
        Url url = new QUrl().id
                .equalTo(id)
                .findOne();
        if (url == null) {
            throw new NotFoundResponse();
        }
        try {
            HttpResponse<String> response = Unirest.get(url.getName()).asString();
            Document document = Jsoup.parse(response.getBody());
            String title = (document.title().isEmpty() ? "отсутствует" : document.title());
            Element h1Element = document.selectFirst("h1");
            String h1 = h1Element != null && h1Element.hasText() ? h1Element.text() : "отсутствует";
            Element descriptionElement = document.selectFirst("meta[name=description]");
            String descriptionContent = descriptionElement != null ? descriptionElement
                    .attr("content") : "";
            String description = (!descriptionContent.isEmpty() ? descriptionContent
                    : "отсутствует");
            UrlCheck check = new UrlCheck(response.getStatus(), title, h1, description, url);
            check.save();
            url.addCheck(check);
            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.redirect("/urls/" + id);

        } catch (Exception e) {
            LOGGER.error("error", e);
            ctx.sessionAttribute("flash", "проверка не удалась, что то не так с сайтом =(");
            ctx.redirect("/urls/" + id);
        }
    };
}

