package com.example.wallpaper.data.repository.util;

import com.example.wallpaper.data.network.model.NetworkWallhavenTag;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class WallhavenTagsDocumentParser {
    private WallhavenTagsDocumentParser() {}

    public static List<NetworkWallhavenTag> parsePopularTags(Document doc) {
        Element tagListDiv = doc.selectFirst("div#taglist");
        if (tagListDiv == null) return List.of();

        Elements tagMains = tagListDiv.select("div.taglist-tagmain");
        List<NetworkWallhavenTag> tags = new ArrayList<>();

        for (Element ele : tagMains) {
            Element nameSpan = ele.selectFirst("span.taglist-name");
            if (nameSpan == null) continue;

            Element nameAnchor = nameSpan.selectFirst("a");
            if (nameAnchor == null) continue;

            String href = nameAnchor.attr("href");
            long id = parseIdFromPath(href);
            if (id == -1) continue;

            String name = nameSpan.text();
            String purity = nameAnchor.hasClass("nsfw")
                    ? "nsfw"
                    : nameAnchor.hasClass("sketchy")
                    ? "sketchy"
                    : "sfw";

            Element categoryChain = ele.selectFirst("span.taglist-category");
            Element categoryAnchor = (categoryChain != null)
                    ? categoryChain.select("a").last()
                    : null;

            String category = categoryAnchor != null
                    ? categoryAnchor.text()
                    : "";

            long categoryId = (categoryAnchor != null)
                    ? parseIdFromPath(categoryAnchor.attr("href"))
                    : 0L;

            Element creatorSpan = ele.selectFirst("span.taglist-creator");
            String createdAtStr = (creatorSpan != null)
                    ? creatorSpan.selectFirst("time").attr("datetime")
                    : null;

            Instant createdAt;
            try {
                createdAt = (createdAtStr != null)
                        ? Instant.parse(createdAtStr)
                        : Instant.now();
            } catch (Exception e) {
                createdAt = Instant.now();
            }

            tags.add(new NetworkWallhavenTag(
                    id,
                    name,
                    "",
                    categoryId,
                    category,
                    purity,
                    createdAt
            ));
        }

        return tags;
    }

    private static long parseIdFromPath(String href) {
        try {
            String path = new URL(href).getPath();
            String[] parts = path.split("/");
            return parts.length >= 3
                    ? Long.parseLong(parts[2])
                    : -1L;
        } catch (Exception e) {
            return -1L;
        }
    }
}
