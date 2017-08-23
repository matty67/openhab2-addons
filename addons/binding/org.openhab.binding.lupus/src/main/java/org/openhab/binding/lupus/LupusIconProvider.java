/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lupus;

import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Set;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.smarthome.ui.icon.AbstractResourceIconProvider;
import org.eclipse.smarthome.ui.icon.IconProvider;
import org.eclipse.smarthome.ui.icon.IconSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LupusIconProvider} is responsible for delivering extra icons in svg and png format.
 *
 * @author Martin Schlaak - Initial contribution
 */
public class LupusIconProvider extends AbstractResourceIconProvider implements IconProvider {

    private final Logger logger = LoggerFactory.getLogger(LupusIconProvider.class);

    @Override
    public Set<IconSet> getIconSets(Locale locale) {
        return null;
    }

    @Override
    protected Integer getPriority() {
        return 0;
    }

    @Override
    protected InputStream getResource(String iconSetId, String resourceName) {
        String ext = FilenameUtils.getExtension(resourceName).toLowerCase();
        String base = FilenameUtils.getBaseName(resourceName);
        String svgFile = base + ".svg";
        URL iconResource = context.getBundle().getEntry("icons/" + svgFile);
        try {
            // Convert SVG to PNG
            InputStream in = iconResource.openStream();
            if (ext.equals("png")) {
                PipedInputStream pin = new PipedInputStream();
                PipedOutputStream out = new PipedOutputStream(pin);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            TranscoderInput inputSvgImage = new TranscoderInput(in);
                            TranscoderOutput outputPngImage = new TranscoderOutput(out);
                            PNGTranscoder myConverter = new PNGTranscoder();
                            myConverter.transcode(inputSvgImage, outputPngImage);
                        } catch (TranscoderException e) {
                            logger.error("png transcode error", e);
                        }
                    }
                }).start();
                return pin;
            } else {
                return in;
            }

        } catch (Exception e) {
            logger.error("Failed to read icon '{}': {}", resourceName, e.getMessage());
            return null;
        }
    }

    @Override
    protected boolean hasResource(String iconSetId, String resourceName) {
        String ext = FilenameUtils.getExtension(resourceName).toLowerCase();
        String base = FilenameUtils.getBaseName(resourceName);
        String svgFile = base + ".svg";
        // Return True for png
        boolean bExists = context.getBundle().getEntry("icons/" + resourceName) != null;
        if (ext.equals("png")) {
            bExists = context.getBundle().getEntry("icons/" + svgFile) != null;
        }
        if (bExists) {
            return true;
        }
        logger.debug("Missing resource {}", resourceName);
        return false;
    }

}
