/**
 * mpg2dcm by Tom Doel
 *
 * http://github.com/tomdoel/mpg2dcm
 *
 * Distributed under the MIT License
 */

package com.tomdoel.mpg2dcm;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class used to parse the XML file accompanying endoscopic video captures
 *
 * <p>Part of <a href="http://github.com/tomdoel/mpg2dcm">mpg2dcm</a>
 *
 * @author Tom Doel
 * @version 1.0
 */
public class EndoscopicXmlParser extends DefaultHandler {
    enum Mode {NONE, IMAGES, VIDEOS, SOUNDS}

    private Map<String, String> map = null;
    private String currentKey = null;
    private StringBuilder currentValue = null;
    private List<String> videoFiles = null;
    private List<String> videoThumbnailFiles = null;
    private List<String> pictureFiles = null;
    private List<String> pictureFileHints = null;
    private List<String> pictureFileAudio = null;
    private List<String> soundFiles = null;
    private Mode mode = Mode.NONE;

    public void startDocument() throws SAXException {
        map = new HashMap<String, String>();
        videoFiles = new ArrayList<String>();
        videoThumbnailFiles = new ArrayList<String>();
        pictureFiles = new ArrayList<String>();
        pictureFileHints = new ArrayList<String>();
        pictureFileAudio = new ArrayList<String>();
        soundFiles = new ArrayList<String>();
    }

    public void endDocument() throws SAXException {
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        currentKey = qName;
        currentValue = new StringBuilder();
        if (qName.equals("Images")) {
            mode = Mode.IMAGES;
        } else if (qName.equals("Videos")) {
            mode = Mode.VIDEOS;
        } else if (qName.equals("Sounds")) {
            mode = Mode.SOUNDS;
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (mode == Mode.VIDEOS) {
            if (currentKey.equals("File")) {
                videoFiles.add(currentValue.toString());
            } else if (currentKey.equals("Thumb")) {
                videoThumbnailFiles.add(currentValue.toString());
            }
        } else if (mode == Mode.IMAGES) {
                if (currentKey.equals("File")) {
                    pictureFiles.add(currentValue.toString());
                } else if (currentKey.equals("Hint")) {
                    pictureFileHints.add(currentValue.toString());
                } else if (currentKey.equals("AUDIO")) {
                    pictureFileAudio.add(currentValue.toString());
                }
        } else {
            map.put(currentKey, currentValue.toString());
        }
        if (qName.equals("Images")) {
            mode = Mode.NONE;
        } else if (qName.equals("Videos")) {
            mode = Mode.NONE;
        } else if (qName.equals("Sounds")) {
            mode = Mode.NONE;
        }
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        if (currentValue != null) {
            currentValue.append(ch, start, length);
        }
    }

    public Map<String, String> getTagMap() {
        return map;
    }

    public List<String> getVideoFilenames() {
        return videoFiles;
    }

    public List<String> getPictureFilenames() {
        return pictureFiles;
    }

    public List<String> getSoundFilenames() {
        return soundFiles;
    }
}