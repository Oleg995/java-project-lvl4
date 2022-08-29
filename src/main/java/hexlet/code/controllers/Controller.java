package hexlet.code.controllers;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.model.query.QUrl;
import hexlet.code.parser.ParserUrl;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.List;

public final class Controller {
    private static Logger logger = LoggerFactory.getLogger(Controller.class);
    public static Handler addToBase = ctx -> {
        String input = ctx.formParam("url");
//        Validator<String> urlValidator = ctx.formParamAsClass("firstName", String.class)
//                .check(str -> !str.isEmpty(), "line not is null");
        try {
            Url url = new Url(ParserUrl.parse(input));
            boolean existUrl = new QUrl().name.equalTo(url.getName()).exists();
            if (existUrl) {
                ctx.sessionAttribute("flashWarning", "страница уже создана");
                ctx.redirect("/urls");
            } else {
                url.save();
                ctx.sessionAttribute("flashSuccess", "страница успешно добавлена");
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
        ctx.attribute("url", url);
        ctx.render("urls/show.html");
    };

    public static Handler createCheck = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);
        Url url = new QUrl().id
                .equalTo(id)
                .findOne();
        try {
            HttpResponse<String> response = Unirest.get(url.getName()).asString();
            Document document = Jsoup.parse(response.getBody());
            UrlCheck check = new UrlCheck();
            check.setStatusCode(response.getStatus());
            check.setUrl(url);
            try {
                check.setTitle(document.title());
            } catch (NullPointerException e) {
                check.setTitle("отсутствует");
            }
            try {
                check.setH1(document.selectFirst("h1").text());
            } catch (NullPointerException e) {
                check.setH1("отсутствует");
            }
            try {
                check.setDescription(document.selectFirst("meta[name=description]").attr("content"));
            } catch (NullPointerException e) {
                check.setDescription("отсутствует");
            }
            check.save();
            url.addCheck(check);
            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.redirect("/urls/" + id);

        } catch (Exception e) {
            logger.error("error", e);
            ctx.sessionAttribute("flash", "проверка не удалась, что то не так с сайтом =(");
            UrlCheck check = new UrlCheck();
            check.setStatusCode(404);
            check.setUrl(url);
            check.save();
            ctx.redirect("/urls/" + id);
        }
    };
}

