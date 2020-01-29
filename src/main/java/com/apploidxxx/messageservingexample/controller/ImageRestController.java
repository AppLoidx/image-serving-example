package com.apploidxxx.messageservingexample.controller;

import com.apploidxxx.messageservingexample.entity.Image;
import com.apploidxxx.messageservingexample.entity.ImageRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@RestController
public class ImageRestController {

    private final ImageRepo imageRepo;

    public ImageRestController(ImageRepo imageRepo) {
        this.imageRepo = imageRepo;
    }

    @PostMapping(value = "/image/upload")
    public void uploadImage(
            @RequestParam("id") Long id,
            @RequestParam("image")MultipartFile image,
            HttpServletResponse response    // for redirect user (only for example)
    ) {

        log.info("Receiving new image");

        try {
            saveImage(id, image);
            // redirecting user to image location see below
            log.info("Image saved for customer with id " + id);
            response.sendRedirect("/image?id=" + id);

        } catch (IOException e){
            log.error("We have some trouble here!", e);
        }
    }

    @Transactional
    public void saveImage(Long customerId, MultipartFile image) throws IOException {

        Optional<Image> imageOptional = imageRepo.findByCustomer(customerId);

        // creating a new instance if customer don't have any uploads
        // or if he has we replace old instance with new
        Image imageToStore = imageOptional.orElse(new Image(customerId, wrapBytes(image.getBytes())));
        imageRepo.save(imageToStore);
    }

    // you have to declare Response Body if you use @Controller annotation for this class
    // and when you use @RestController (like here) there is no needed to declare this annotation
    // And you must declare produces media type as Image, because you get array of bytes without this metadata
    @GetMapping(value = "/image", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getImage(@RequestParam("id") Long customerId) {
        Optional<Image> imageOptional = imageRepo.findByCustomer(customerId);
        return imageOptional.map(Image::getImage).map(this::unwrapBytes).orElse(null); // you can handle here something
    }

    // wrap byte to Byte for persist
    private Byte[] wrapBytes(byte[] bytes) {
        Byte[] byteObjects = new Byte[bytes.length];

        int i = 0;
        for (byte b : bytes) {
            byteObjects[i++] = b;
        }

        return byteObjects;
    }

    // unwrap Byte to byte for convert to image
    private byte[] unwrapBytes(Byte[] bytes){
        byte[] primBytes = new byte[bytes.length];
        int j=0;
        for(Byte b: bytes)
            primBytes[j++] = b;

        return primBytes;
    }

}
