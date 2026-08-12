package org.firstinspires.ftc.teamcode.Crawler.sim;

import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * No-op telemetry for JVM integration tests — the path follower and movement engine
 * call {@code addData}/{@code update} every loop, which this stub absorbs silently.
 */
public class FakeTelemetry implements Telemetry {

    @Override public Item addData(String caption, String format, Object... args) { return null; }
    @Override public Item addData(String caption, Object value) { return null; }
    @Override public <T> Item addData(String caption, Func<T> valueProducer) { return null; }
    @Override public <T> Item addData(String caption, String format, Func<T> valueProducer) { return null; }
    @Override public boolean removeItem(Item item) { return false; }
    @Override public void clear() {}
    @Override public void clearAll() {}
    @Override public Object addAction(Runnable action) { return null; }
    @Override public boolean removeAction(Object token) { return false; }
    @Override public void speak(String text) {}
    @Override public void speak(String text, String languageCode, String countryCode) {}
    @Override public boolean update() { return true; }
    @Override public Line addLine() { return null; }
    @Override public Line addLine(String lineCaption) { return null; }
    @Override public boolean removeLine(Line line) { return false; }
    @Override public boolean isAutoClear() { return true; }
    @Override public void setAutoClear(boolean autoClear) {}
    @Override public int getMsTransmissionInterval() { return 250; }
    @Override public void setMsTransmissionInterval(int msTransmissionInterval) {}
    @Override public String getItemSeparator() { return "|"; }
    @Override public void setItemSeparator(String itemSeparator) {}
    @Override public String getCaptionValueSeparator() { return ":"; }
    @Override public void setCaptionValueSeparator(String captionValueSeparator) {}
    @Override public void setDisplayFormat(DisplayFormat displayFormat) {}
    @Override public Log log() { return null; }
}
