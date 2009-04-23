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
#include <string.h>

#include <minigui/common.h>
#include <minigui/minigui.h>
#include <minigui/gdi.h>
#include <minigui/window.h>
#include <minigui/control.h>

#include <minictrl/mctrls.h>
#include <minictrl/mresmanager.h>

#include "../header/id.h"

const char *project_path = "$(location)";

//extern NCS_EVENT_HANDLERS mainwnd_Test_handlers[];

extern int start_wnd_id ;

int MiniGUIMain(int argc, const char* argv[])
{
	MSG Msg;
    char f_package[MAX_PATH];

	RegisterMiniControls();

    sprintf(f_package, "%s/%s", project_path, "res/$(projectName).res");

    HPACKAGE hPkg = LoadResPackage (f_package);

    mMainWnd *mWin = CreateMainWindowIndirectFromID
        (hPkg, start_wnd_id , HWND_DESKTOP, 0, 0,
         NULL);
        //mainwnd_Test_handlers);

    while(GetMessage(&Msg, mWin->hwnd))
	{
		TranslateMessage(&Msg);
		DispatchMessage(&Msg);
	}

	MainWindowThreadCleanup(mWin->hwnd);
    UnloadResPackage(hPkg);
    return 0;
}

#ifdef _MGRM_THREADS
#include <minigui/dti.c>
#endif
