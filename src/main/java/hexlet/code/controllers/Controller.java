package hexlet.code.controllers;

import hexlet.code.model.ParserUrl;
import hexlet.code.model.Url;
import hexlet.code.model.query.QUrl;
import io.ebean.PagedList;
import io.javalin.http.Handler;

import java.net.MalformedURLException;
import java.util.List;

public final class Controller {
    public static Handler addToBase = ctx -> {
        String input = ctx.formParam("input");
        try {
            Url url = new Url(ParserUrl.url(input));
            boolean existUrl = new QUrl().name.equalTo(url.getName()).exists();
            if (existUrl) {
                ctx.sessionAttribute("flash", "страница уже создана");
                ctx.redirect("/urls");
            } else {
                url.save();
                ctx.sessionAttribute("flash", "страница успешно добавлена");
                ctx.redirect("/urls");
            }
        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flash", "некорректный url");
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
}
