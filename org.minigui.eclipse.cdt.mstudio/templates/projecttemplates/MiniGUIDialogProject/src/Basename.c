/*
 ============================================================================
 Name        : $(baseName).c
 Author      : $(author)
 Version     :
 Copyright   : $(copyright)
 Description : Main Window in MiniGUI
 ============================================================================
 */

#include <stdio.h>
#include <stdlib.h>

#include <minigui/common.h>
#include <minigui/minigui.h>
#include <minigui/gdi.h>
#include <minigui/window.h>
#include <minigui/control.h>

static CTRLDATA CtrlData [] =
{
    {
        "button",
        WS_TABSTOP | WS_VISIBLE | BS_DEFPUSHBUTTON,
        10, 10, 60, 25,
        IDOK,
        "OK",
        0
    },
};

static DLGTEMPLATE DlgTempl =
{
    WS_BORDER | WS_CAPTION,
    WS_EX_NONE,
    0, 0, 0, 0,
    "$(projectName)",
    0, 0,
    sizeof(CtrlData)/sizeof(CTRLDATA),
    CtrlData,
    0
};

static int InitDialogProc (HWND hDlg, int message, WPARAM wParam, LPARAM lParam)
{
    switch (message) {

        case MSG_INITDIALOG:
            return 1;

        case MSG_COMMAND:
            switch (wParam) {
                case IDOK:
                    EndDialog (hDlg, wParam);
                    break;
                                }
            break;

        case MSG_CLOSE:
            EndDialog (hDlg, IDCANCEL);
            break;
    }

    return DefaultDialogProc (hDlg, message, wParam, lParam);
}

int MiniGUIMain (int argc, const char* argv[])
{
#ifdef _MGRM_PROCESSES
    JoinLayer(NAME_DEF_LAYER , "$(message)" , 0 , 0);
#endif

    DlgTempl.w = RECTW(g_rcScr);
    DlgTempl.h = RECTH(g_rcScr);
    DialogBoxIndirectParam (&DlgTempl, HWND_DESKTOP, InitDialogProc, 0L);

    return 0;
}

#ifdef _MGRM_THREADS
#include <minigui/dti.c>
#endif
