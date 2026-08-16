package de.haevn.worksuite.about;


import de.haevn.worksuite.common.RestApiController;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;

@Log4j2
@RequiredArgsConstructor
@RestApiController("/api/v1/about")
public class AboutController {
    private final AboutService aboutService;

    @GetMapping
    public AboutSystemInfoResponse getSystemInfo() {
        return aboutService.getSystemInfo();
    }


    @GetMapping("/ping")
    public String ping() {

        return "pongs";
    }

}
