/*
** This file is a part of miniStudio, which provides a WYSIWYG UI designer
** and an IDE for MiniGUI app developers.
**
** Copyright (C) 2010 ~ 2019, Beijing FMSoft Technologies Co., Ltd.
**
** This program is free software: you can redistribute it and/or modify
** it under the terms of the GNU General Public License as published by
** the Free Software Foundation, either version 3 of the License, or
** (at your option) any later version.
**
** This program is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
** GNU General Public License for more details.
**
** You should have received a copy of the GNU General Public License
** along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
