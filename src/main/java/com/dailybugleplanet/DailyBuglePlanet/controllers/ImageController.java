/*
// Curso Egg FullStack
 */
package com.dailybugleplanet.DailyBuglePlanet.controllers;

// @author Ramiro Aybar

import com.dailybugleplanet.DailyBuglePlanet.entities.Account;
import com.dailybugleplanet.DailyBuglePlanet.entities.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import com.dailybugleplanet.DailyBuglePlanet.repositories.AccountRepository;
import com.dailybugleplanet.DailyBuglePlanet.repositories.ImageRepository;


@Controller
@RequestMapping("/image")
public class ImageController {

    @Autowired
    AccountRepository userRepository;

    @Autowired
    ImageRepository imageRepository;

    @GetMapping("/profile/{id}")
    public ResponseEntity<byte[]> userImage(@PathVariable String id) {
        Account user = userRepository.searchAccountById(id);

        byte[] image = user.getImage().getContent();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);

        return new ResponseEntity(image, headers, HttpStatus.OK);
    }

    @GetMapping("/image/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable String id) {
        Image res = imageRepository.searchById(id);

        byte[] foto = res.getContent();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);

        return new ResponseEntity(foto, headers, HttpStatus.OK);

    }

}
