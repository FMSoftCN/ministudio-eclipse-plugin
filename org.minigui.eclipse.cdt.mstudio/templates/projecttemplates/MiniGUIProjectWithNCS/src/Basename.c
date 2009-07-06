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

#include <mgncs/mgncs.h>
#include <mgncs/mgncs_resmanager.h>

#include "resource.h"
#include "ncs-windows.h"

const char *project_path = "$(location)";

int MiniGUIMain(int argc, const char* argv[])
{
	MSG Msg;
	char f_package[MAX_PATH];
	mMainWnd *mWin;
	HPACKAGE hPkg;

	RegisterMiniControls();
	sprintf(f_package, "%s/%s", project_path, "$(projectName)/res/$(projectName).res");
	SetResPath("./");

	hPkg = LoadResPackage (f_package);
	if (hPkg == HPACKAGE_NULL)
		return 1;

	SetDefaultWindowElementRenderer(GetString(hPkg, MGRM_SYSSTR_DEFRDR));

	mWin = ntStartWindowEx(hPkg, HWND_DESKTOP, (HICON)0, (HMENU)0, (DWORD)0);

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
