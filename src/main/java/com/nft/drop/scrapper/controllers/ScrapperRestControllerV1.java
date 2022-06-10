package com.nft.drop.scrapper.controllers;

import com.nft.drop.scrapper.models.Nft;
import com.nft.drop.scrapper.service.ScrapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/nft")
public class ScrapperRestControllerV1 {

    private final ScrapperService scrapper;

    @Autowired
    public ScrapperRestControllerV1(ScrapperService scrapper) {
        this.scrapper = scrapper;
    }

    @GetMapping("/scrap")
    private List<Nft> scrap() {
        return scrapper.scrap();
    }

}
