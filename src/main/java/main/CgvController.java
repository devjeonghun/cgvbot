package main;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Config;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CgvController extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // selenium
    private WebDriver driver;
    private WebElement webElement;
    private JavascriptExecutor jse;

    private List<MovieInfo> checkedMovieInfoList = new ArrayList<>();
    public boolean flag = false;

    CgvController(int type) {
        init(type);
    }

    @Override
    public void run() {
        login(Config.properties.getProperty("CGV_ID"), Config.properties.getProperty("CGV_PW"));
        while (flag) {
            try {
                if (goMovieList("0013")) {

                    if (checkMovieList()) {
                        break;
                    }

                }
                Thread.sleep(Integer.parseInt(Config.properties.getProperty("RETRY_TIME")) * 1000);
            } catch (Exception e) {

            }
        }
    }

    /**
     * init
     *
     * @param type
     */
    public void init(int type) {
        System.setProperty("webdriver.chrome.driver", Config.SYSTEM_ROOT + "/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        switch (type) {
            case 1:
                options.addArguments("headless");
                options.addArguments("window-size=1920x1080");
                options.addArguments("disable-gpu");
                break;
            default:
                break;
        }
        this.driver = new ChromeDriver(options);
        /*this.driver.manage().window().setSize(new Dimension(200, 500));*/
        this.driver.manage().timeouts().implicitlyWait(50, TimeUnit.SECONDS);
        this.jse = (JavascriptExecutor) driver;
        LOGGER.info("{}", "init");
    }

    /**
     * login
     *
     * @param id
     * @param password
     */
    public void login(String id, String password) {
        this.driver.get(Config.URL_ROOT + "/WebApp/Member/Login.aspx");
        waitNextPage(By.id("headerTitleArea"));
        webElement = driver.findElement(By.id("mainContentPlaceHolder_Login_tbUserID"));
        webElement.sendKeys(id);
        webElement = driver.findElement(By.id("mainContentPlaceHolder_Login_tbPassword"));
        webElement.sendKeys(password);
        jse.executeScript("clickLogin();");
        waitNextPage(By.id("navHome"));
        LOGGER.info("{}", "CGV login Complete");
    }

    /**
     * goMovieList
     *
     * @param tc 영화관
     *
     * @return
     */
    protected boolean goMovieList(String tc) {
        boolean result = false;
        try {
            String url = String.format(Config.URL_ROOT + "/Schedule/?tc=%4s&t=T&ymd=%8s&src=", tc, Config.properties.getProperty("SEARCH_DATE"));
            this.driver.navigate().to(url);

            result = isAlertPresent();
            if (result) {
                LOGGER.info("[{}]{}", Config.properties.getProperty("SEARCH_DATE"), Config.properties.getProperty("MOVIE_NAME") + " Not Exist!");
            }
        } catch (UnhandledAlertException e) {
            return false;
        }

        return !result;
    }

    /**
     * checkMovieList
     *
     * @param date
     *
     * @return
     *
     * @throws InterruptedException
     */
    public boolean checkMovieList() throws InterruptedException {
        waitNextPage(By.id("ScheduleDateLayer"));
        List<WebElement> movieTime = driver.findElements(By.className("movieTime"));
        StringBuilder sb = new StringBuilder();
        List<MovieInfo> checkMovie = new ArrayList<>();

        // 전체 영화 리스트 Loop
        for (WebElement title : movieTime) {
            if (title.findElement(By.className("tit")).getText().contains(Config.properties.getProperty("MOVIE_NAME"))) {
                List<WebElement> titleList = title.findElements(By.className("time"));
                LOGGER.info(title.findElement(By.className("tit")).getText());
                titleList.remove(titleList.size() - 1);
                // 선택된 영화의 상영관별 Loop
                for (WebElement time : titleList) {
                    String mc = time.findElements(By.tagName("span")).get(2).getText();
                    if (time.findElement(By.className("lo_h")).getText().contains(Config.properties.getProperty("MOVIE_TYPE"))) {
                        List<WebElement> li = time.findElements(By.tagName("li"));
                        // 상영관의 상영시간 Loop
                        for (WebElement l : li) {
                            if (l.getSize().width == 47 && l.getText().contains(":")) {
                                int screenTime = Integer.parseInt(l.getText().replace(":", ""));
                                int checkStrTime = Integer.parseInt(Config.properties.getProperty("SEARCH_STR_TIME"));
                                int checkEndTime = Integer.parseInt(Config.properties.getProperty("SEARCH_END_TIME"));
                                if (checkStrTime <= screenTime && checkEndTime >= screenTime) {
                                    MovieInfo info = new MovieInfo();
                                    info.setMc(mc);
                                    info.setPlayTime(screenTime);
                                    info.setWebElement(l);
                                    checkMovie.add(info);
                                    sb.append(String.format("-%s/%s", mc, l.getText()) + "\n");
                                }
                            }
                        }
                    }
                }
            }
        }
        if (checkMovie.size() > 0) {
            String msg = String.format("%s[%s]%s", Config.properties.getProperty("MOVIE_NAME"), Config.properties.getProperty("SEARCH_DATE"), "\n" + sb.toString());
            LOGGER.info(msg);
            //pushLine(msg);
            List<MovieInfo> sort = checkMovie.stream().sorted(Comparator.comparing(MovieInfo::getPlayTime)).collect(Collectors.toList());

            // 기존에 체크한 리스트가 있을때
            if (checkedMovieInfoList.size() > 0 && checkMovie.size() > checkedMovieInfoList.size()) {
                checkedMovieInfoList.forEach(checked -> {
                    sort.removeIf(check -> checked.getMc().equalsIgnoreCase(check.getMc()) && checked.getPlayTime() == check.getPlayTime());
                });
            }

            try {
                MovieInfo info = sort.get(0);
                LOGGER.info("{}", info.getMc() + "/" + info.getWebElement().getText());
                //pushLine(info.getMc() + "/" + info.getWebElement().getText());

                info.getWebElement().click();
                Thread.sleep(500);
                driver.findElement(By.className("schedulePopup")).findElements(By.tagName("ul")).get(1).findElements(By.tagName("a")).get(0).click();
                if (seat()) {
                    if (payInit()) {
                        return true;
                    }
                } else {
                    checkedMovieInfoList.add(info);
                    return false;
                }
            } catch (Exception e) {

            }
            return true;
        } else {
            LOGGER.info("{}", "Not Exist." + "[" + Config.properties.getProperty("SEARCH_DATE") + "]");
            return false;
        }
    }

    public boolean seat() throws InterruptedException {
        boolean result = false;

        try {
            (new WebDriverWait(driver, 1)).until(ExpectedConditions.alertIsPresent());
            isAlertPresent();
            (new WebDriverWait(driver, 1)).until(ExpectedConditions.alertIsPresent());
            isAlertPresent();
            (new WebDriverWait(driver, 1)).until(ExpectedConditions.alertIsPresent());
            isAlertPresent();
        } catch (Exception e) {

        }
        /*Select seatCnt = new Select(driver.findElement(By.id("General")));
        seatCnt.selectByIndex(Integer.parseInt(Config.properties.getProperty("SEAT_NUM")));*/
        jse.executeScript("window.alert = function(msg) {};");
        jse.executeScript("_alert=alert;");
        jse.executeScript("$('#General').val(1).trigger('change')");
        List<WebElement> seatList = (List<WebElement>) jse.executeScript("return $('#seat_table td[reservation=Yes]');");
        switch (Config.properties.getProperty("SEAT_SELECT_TYPE")) {
            case "1": //좌석타입
                for (WebElement a : seatList) {
                    if (a.getAttribute("rating_nm").equalsIgnoreCase(Config.properties.getProperty("SEAT_TYPE"))) {
                        webElement = a;
                        result = true;
                        break;
                    }
                }
                if (!result) {
                    //pushLine(Config.properties.getProperty("SEAT_TYPE") + " 없음");
                    LOGGER.info(Config.properties.getProperty("SEAT_TYPE") + " 없음");
                    return false;
                }
                break;
            case "2": //좌석Row
                break;
            case "3": //좌석Row, Col
                break;
            case "4": //확정 좌석
                break;
            case "5": //아무 좌석
                boolean loopflag = false;
                while(loopflag){
                    for (WebElement a : seatList) {
                        if (a.getAttribute("rating_nm").equalsIgnoreCase(Config.properties.getProperty("SEAT_TYPE")) &&
                                !a.getAttribute("seatname").equals("") &&
                                !a.getAttribute("class").equals("yesok")) {
                            webElement = a;
                            result = true;
                            loopflag = true;
                            break;
                        }
                    }
                    if (!result) {
                        LOGGER.info(Config.properties.getProperty("SEAT_TYPE") + " 없음");
                        return false;
                    }
                }
                break;
                default:

                break;
        }

        String seatName = webElement.getAttribute("seatname");
        jse.executeScript("$('#seat_table td[seatname=" + webElement.getAttribute("seatname") + "]').click();");
        Thread.sleep(1000);
        jse.executeScript("submitSeat();");
        try {
            (new WebDriverWait(driver, 2)).until(ExpectedConditions.alertIsPresent());
            isAlertPresent();
        } catch (Exception e) {

        }
        LOGGER.error("{}", "Seat Checkin Complete.");
        pushLine(seatName + "  좌석 선택 완료");
        pushTelegram(seatName + "  좌석 선택 완료");
        return result;
    }

    public boolean payInit() throws InterruptedException {
        LOGGER.error("{}", "Pay init.");
        pushLine("PayInit");
        pushTelegram("PayInit");
        boolean result = false;
        jse.executeScript("$('li[class=kakao_pay] > a').trigger('click');");
        jse.executeScript("$('#btn_payment').trigger('click');");
        waitNextPage(By.className("logo_pay"));
        driver.findElement(By.className("off")).findElement(By.tagName("a")).click();
        driver.findElement(By.id("userPhone")).sendKeys(Config.properties.getProperty("MOBILE_NUM"));
        driver.findElement(By.id("userBirth")).sendKeys(Config.properties.getProperty("BIRTHDAY"));
        driver.findElement(By.className("btn_payask")).click();

        waitNextPage(By.className("btn_submit"));
        while (true) {
            LOGGER.error("{}", "Pay waiting...");
            pushLine("Pay waiting...");
            pushTelegram("Pay waiting...");
            driver.findElement(By.className("btn_submit")).click();
            if (driver.findElements(By.id("alertOkButton")).size() < 0) {
                result = true;
                break;
            } else {
                Thread.sleep(1000);
                driver.findElement(By.id("alertOkButton")).click();
            }
            Thread.sleep(10000);
        }
        LOGGER.error("{}", "Pay Complete.");
        pushLine("Pay Complete.");
        pushTelegram("Pay Complete.");
        return result;
    }


    /**
     * Line Push
     *
     * @param message
     */
    public void pushLine(String message) {
        if (Config.properties.getProperty("MESSAGE_TYPE").equalsIgnoreCase("LINE")) {

        }
        try {
            URL obj = new URL(Config.LINE_URL);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Bearer " + Config.properties.getProperty("LINE_TOKEN"));
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            con.setConnectTimeout(10000);
            con.setReadTimeout(10000);
            con.setDoInput(true);
            con.setDoOutput(true);
            Map<String, Object> params = new LinkedHashMap<>();

            params.put("message", message);

            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (postData.length() != 0)
                    postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            OutputStream os = con.getOutputStream();
            os.write(postDataBytes);
            os.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer rs = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                rs.append(inputLine);
            }
            in.close();
        } catch (Exception e) {

        }
    }
    public void pushTelegram(String message) {
        try {
            URL obj = new URL("https://api.telegram.org/bot/sendMessage");
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            /*con.setRequestProperty("Authorization", "Bearer " + Config.properties.getProperty("LINE_TOKEN"));
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");*/

            con.setConnectTimeout(10000);
            con.setReadTimeout(10000);
            con.setDoInput(true);
            con.setDoOutput(true);
            Map<String, Object> params = new LinkedHashMap<>();

            params.put("chat_id", "816824094");
            params.put("text",message);
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (postData.length() != 0)
                    postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            OutputStream os = con.getOutputStream();
            os.write(postDataBytes);
            os.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer rs = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                rs.append(inputLine);
            }
            in.close();
        } catch (Exception e) {

        }
    }
    public boolean isAlertPresent() {
        try {
            driver.switchTo().alert().accept();
            return true;
        }   // try
        catch (NoAlertPresentException Ex) {
            return false;
        }
    }

    protected void waitNextPage(By value) {
        (new WebDriverWait(driver, 30)).until(ExpectedConditions.elementToBeClickable(value));
        LOGGER.info("{}", "page navigating....");
    }

    protected void getPageScreenShot() {
        try {
            File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(scrFile, new File(Config.SYSTEM_ROOT + "/screenshot.png"));
        } catch (Exception e) {

        }
    }
}