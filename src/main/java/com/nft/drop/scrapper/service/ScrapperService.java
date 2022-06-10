package com.nft.drop.scrapper.service;

import com.nft.drop.scrapper.constants.URLs;
import com.nft.drop.scrapper.models.Nft;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScrapperService {

    private final WebClient webClient;

    @Autowired
    public ScrapperService(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<Nft> scrap() {
        return scrapNFTs(nftTodayLinks(nftTodayHtmlContent()));
    }

    private List<Nft> scrapNFTs(List<String> nftLinks) {
        List<String> nftDocuments = Flux.fromIterable(nftLinks)
                .flatMap(nftLink -> webClient.get().uri(URLs.DOMAIN_URL + nftLink).retrieve().bodyToMono(String.class))
                .collect(Collectors.toList())
                .block();
        return Flux.fromIterable(nftDocuments)
                .flatMap(nftDocument -> {
                    Elements nftCardElements = Jsoup.parse(nftDocument).select(".next-drop-wrapper");
                    String imageLink = nftCardElements.select("img").attr("src");
                    String name = nftCardElements.select(".nft-project-title").text();
                    String description = nftCardElements.select(".nft-description").text();
                    Elements detailElements = nftCardElements.select(".project-details div:not(.w-condition-invisible).div-next-drops-info");
                    Elements tagsElements = nftCardElements.select(".tags");
                    List<String> tags = tagsElements.stream().map(tag -> String.join("_", tag.text().toLowerCase().split("\s+"))).toList();
                    List<String> details = parseDetails(detailElements);
                    Elements linksElements = nftCardElements.select(".project-links div:not(.w-condition-invisible).div-next-drops-info");
                    List<String> links = parseLinks(linksElements);
                    return Mono.just(new Nft(imageLink, name, details, links, description, tags));
                }).collect(Collectors.toList()).block();
    }

    private List<String> parseDetails(Elements elements) {
        return elements.stream()
                .map(element ->
                        element.select(".title").text().trim() + " " +
                                element.select(".stats").text().trim() + " " +
                                element.select(".suffix").text().trim())
                .toList();
    }

    private List<String> parseLinks(Elements elements) {
        return elements.stream()
                .map(element ->
                        {
                            Elements a = element.select("a[href^=http]:lt(1)");
                            String href = a.attr("href");
                            return Arrays.stream(a.text().split("\s+")).map(text ->
                                    switch (text) {
                                        case "Twitter:", "Website:", "Discord:" -> String.format("<a href='%s'>%s</a>", href, text.trim());
                                        default -> "";
                                    }).collect(Collectors.joining());
                        }
                )
                .filter(nextElement -> !nextElement.isEmpty())
                .toList();
    }


    private String nftTodayHtmlContent() {
        return webClient.get()
                .uri(URLs.NFT_TODAY_URL)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private List<String> nftTodayLinks(String html) {
        return Jsoup.parse(html)
                .select(String.format(".product-detail .moobs a[href^=%s]", URLs.NFT_DROPS_URL))
                .eachAttr("href");
    }

}
