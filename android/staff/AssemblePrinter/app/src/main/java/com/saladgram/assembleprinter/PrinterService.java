package com.saladgram.assembleprinter;

import android.content.Context;
import android.util.Log;

import com.bxl.config.editor.BXLConfigLoader;
import java.util.List;

import jpos.JposException;
import jpos.POSPrinter;
import jpos.POSPrinterConst;
import jpos.config.JposEntry;

/**
 * Created by yns on 7/10/16.
 */
public class PrinterService {
    private static Context sContext;
    private static POSPrinter posPrinter;

    public static void start(Context applicationContext) {
        sContext = applicationContext;
        if (posPrinter == null) {
            posPrinter = new POSPrinter(sContext);
            config();
        }
    }

    private static void config() {
        BXLConfigLoader bxlConfigLoader = new BXLConfigLoader(sContext);

        try {
            List<?> entries = null;
            entries = bxlConfigLoader.getEntries();
            for (Object entry : entries) {
                Log.d("yns", ((JposEntry) entry).getLogicalName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            bxlConfigLoader.openFile();
        } catch (Exception e) {
            e.printStackTrace();
            bxlConfigLoader.newFile();
        }
        bxlConfigLoader.removeAllEntries();
        bxlConfigLoader.addEntry("SRP-350III", BXLConfigLoader.DEVICE_CATEGORY_POS_PRINTER, "SRP-350III",BXLConfigLoader.DEVICE_BUS_USB, "");
        try {
            bxlConfigLoader.saveFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void doPrint(String buffer) {
        try {
            posPrinter.open("SRP-350III");
            posPrinter.claim(0);
            posPrinter.setDeviceEnabled(true);

            posPrinter.printNormal(POSPrinterConst.PTR_S_RECEIPT, buffer);

            posPrinter.cutPaper(90);
        } catch (JposException e) {
            e.printStackTrace();
        }
        try {
            posPrinter.release();
        } catch (JposException e) {
            e.printStackTrace();
        }
        try {
            posPrinter.close();
        } catch (JposException e) {
            e.printStackTrace();
        }
    }

}
