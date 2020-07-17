package com.farthestgate.android.ui;

import com.seikoinstruments.sdk.thermalprinter.PrinterException;

/**
 * Created by Hanson on 28/07/2014.
 */
public interface PrinterCommInterface {
    void OnPrinterError(PrinterException pe, boolean reprint);
    void OnPrinterSuccess(boolean reprint);
}
