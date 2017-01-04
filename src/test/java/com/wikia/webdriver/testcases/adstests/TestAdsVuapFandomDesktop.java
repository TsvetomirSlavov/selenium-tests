package com.wikia.webdriver.testcases.adstests;

import com.wikia.webdriver.common.contentpatterns.AdsFandomContent;
import com.wikia.webdriver.common.core.Assertion;
import com.wikia.webdriver.common.core.annotations.NetworkTrafficDump;
import com.wikia.webdriver.common.dataprovider.ads.FandomAdsDataProvider;
import com.wikia.webdriver.common.templates.fandom.AdsFandomTestTemplate;
import com.wikia.webdriver.pageobjectsfactory.componentobject.ad.VideoFanTakeover;
import com.wikia.webdriver.pageobjectsfactory.pageobject.adsbase.AdsFandomObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

@Test(
        groups = "AdsVuapFandomDesktop"
)
public class TestAdsVuapFandomDesktop extends AdsFandomTestTemplate{
    private static final String URL_FIRSTQUARTILE = "ad_vast_point=firstquartile";
    private static final String URL_MIDPOINT = "ad_vast_point=midpoint";
    private static final int DELAY = 2;

    private VideoFanTakeover prepareSlot(String slotName, String iframeId, AdsFandomObject fandomPage) {
        fandomPage.triggerOnScrollSlots();
        VideoFanTakeover videoFanTakeover = new VideoFanTakeover(driver, iframeId);
        fandomPage.scrollToSlot(AdsFandomContent.getSlotSelector(slotName));
        return videoFanTakeover;
    }

    @Test(
            dataProviderClass = FandomAdsDataProvider.class,
            dataProvider = "fandomVuapPage",
            groups = "AdsVideoClosedAfterPlayingFandom"
    )
    public void adsVideoClosedAfterPlayingFandom(String pageType, String pageName, String slotName, String iframeId) {
        AdsFandomObject fandomPage = loadPage(pageName, pageType);
        VideoFanTakeover videoFanTakeover = prepareSlot(slotName, iframeId, fandomPage);

        videoFanTakeover.play();

        videoFanTakeover.waitForVideoStart(slotName);
        videoFanTakeover.waitForVideoPlayerHidden(slotName);
    }

    @Test(
            dataProviderClass = FandomAdsDataProvider.class,
            dataProvider = "fandomVuapPage",
            groups = "AdsImageClickedOpensNewPageFandom"
    )
    public void adsImageClickedOpensNewPageFandom(String pageType, String pageName, String slotName, String iframeId) throws InterruptedException {
        AdsFandomObject fandomPage = loadPage(pageName, pageType);
        VideoFanTakeover videoFanTakeover = prepareSlot(slotName, iframeId, fandomPage);

        videoFanTakeover.clickOnAdImage();
        Assert.assertTrue(fandomPage.tabContainsUrl(VideoFanTakeover.AD_REDIRECT_URL));
    }

    @Test(
            dataProviderClass = FandomAdsDataProvider.class,
            dataProvider = "fandomVuapPage",
            groups = "AdsVuapVideoClosesWhenTapCloseButtonFandom"
    )
    public void adsVuapVideoClosesWhenTapCloseButtonFandom(String pageType, String pageName, String slotName, String iframeId) {
        AdsFandomObject fandomPage = loadPage(pageName, pageType);
        VideoFanTakeover videoFanTakeover = prepareSlot(slotName, iframeId, fandomPage);

        videoFanTakeover.play();
        videoFanTakeover.waitForVideoStart(slotName);
        videoFanTakeover.clickOnVideoCloseButton();
        videoFanTakeover.waitForVideoPlayerHidden(slotName);
    }

    @Test(
            dataProviderClass = FandomAdsDataProvider.class,
            dataProvider = "fandomVuapPage",
            groups = "AdsVuapCheckSlotSizesFamdom"
    )
    public void adsVuapCheckSlotSizesFandom(String pageType, String pageName, String slotName, String iframeId) throws InterruptedException {
        String slotSelector = AdsFandomContent.getSlotSelector(slotName);
        AdsFandomObject fandomPage = loadPage(pageName, pageType);
        fandomPage.triggerOnScrollSlots();
        VideoFanTakeover videoFanTakeover = new VideoFanTakeover(driver, iframeId);
        fandomPage.scrollToSlot(slotSelector);

        videoFanTakeover.waitForAdToLoad();
        double imageHeight = videoFanTakeover.getAdSlotHeight(slotSelector);

        videoFanTakeover.play();

        videoFanTakeover.waitForVideoStart(slotName);
        double videoHeight = videoFanTakeover.getAdVideoHeight(slotName);
        Assertion.assertTrue(videoFanTakeover.isVideoAdBiggerThanImageAdOasis(videoHeight, imageHeight ));

        videoFanTakeover.waitForVideoPlayerHidden(slotName);
        Assertion.assertTrue(videoFanTakeover.isImageAdInCorrectSize(imageHeight, slotSelector));
    }

    @NetworkTrafficDump
    @Test(
            dataProviderClass = FandomAdsDataProvider.class,
            dataProvider = "fandomVuapPage",
            groups = "AdsVuapTimeProgressingFandom"
    )
    public void adsVuapTimeProgressingFandom(String pageType, String pageName, String slotName, String iframeId) throws InterruptedException {
        networkTrafficInterceptor.startIntercepting();
        AdsFandomObject fandomPage = loadPage(pageName, pageType);
        VideoFanTakeover videoFanTakeover = prepareSlot(slotName, iframeId, fandomPage);

        videoFanTakeover.play();

        videoFanTakeover.waitForVideoStart(slotName);
        fandomPage.wait.forSuccessfulResponse(networkTrafficInterceptor, URL_FIRSTQUARTILE);
        double quartileTime = videoFanTakeover.getCurrentVideoTimeOnDesktop(slotName);

        fandomPage.wait.forSuccessfulResponse(networkTrafficInterceptor, URL_MIDPOINT);
        double midTime = videoFanTakeover.getCurrentVideoTimeOnDesktop(slotName);
        Assertion.assertTrue(videoFanTakeover.isTimeProgressing(quartileTime, midTime));
    }

    @NetworkTrafficDump
    @Test(
            dataProviderClass = FandomAdsDataProvider.class,
            dataProvider = "fandomVuapPage",
            groups = "AdsVuapVideoPauseFandom"
    )
    public void adsVuapVideoPausesFandom(String pageType, String pageName, String slotName, String iframeId) throws InterruptedException {
        networkTrafficInterceptor.startIntercepting();
        AdsFandomObject fandomPage = loadPage(pageName, pageType);
        VideoFanTakeover videoFanTakeover = prepareSlot(slotName, iframeId, fandomPage);

        videoFanTakeover.play();

        videoFanTakeover.waitForVideoStart(slotName);
        fandomPage.wait.forSuccessfulResponse(networkTrafficInterceptor, URL_FIRSTQUARTILE);

        videoFanTakeover.pause();

        double time = videoFanTakeover.getCurrentVideoTimeOnDesktop(slotName);

        TimeUnit.SECONDS.sleep(DELAY);

        Assert.assertNotEquals(0, videoFanTakeover.getCurrentVideoTimeOnDesktop(slotName));
        Assert.assertEquals(time, videoFanTakeover.getCurrentVideoTimeOnDesktop(slotName));
    }
}

