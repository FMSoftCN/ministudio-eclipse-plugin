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

#include "resource.h"
#include "ncs-windows.h"

HPACKAGE hPackage = HPACKAGE_NULL;

int MiniGUIMain(int argc, const char* argv[])
{
#ifdef ntStartWindowEx
	MSG Msg;
	char f_package[MAX_PATH];
	mMainWnd *mWin;

	ncsInitialize();

#ifdef _MGNCS_INCORE_RES
	char* pInnerResPackage;
	int innerResPackSize;

	if (ncsGetIncoreResPackInfo(&pInnerResPackage, &innerResPackSize))
		hPackage = ncsLoadResPackageFromMem (pInnerResPackage, innerResPackSize);
	else {
		printf ("Error: get in-core resource package information failure.\n");
		return 1;
	}
#else
	sprintf(f_package, "%s", "res/$(projectName).res");
	SetResPath("./");

	hPackage = ncsLoadResPackage (f_package);
#endif

	if (hPackage == HPACKAGE_NULL) {
#ifdef _MGNCS_INCORE_RES
		printf ("Error: load in-core resource package failure.\n");
#else
		printf ("Error: load resource package:%s failure.\n", f_package);
#endif
		return 1;
	}

	SetDefaultWindowElementRenderer(ncsGetString(hPackage, NCSRM_SYSSTR_DEFRDR));
#ifdef _MGRM_PROCESSES
    JoinLayer(NAME_DEF_LAYER , argv[0], 0 , 0);
#endif

	mWin = ntStartWindowEx(hPackage, HWND_DESKTOP, (HICON)0, (HMENU)0, (DWORD)0);

	while(GetMessage(&Msg, mWin->hwnd))
	{
		TranslateMessage(&Msg);
		DispatchMessage(&Msg);
	}

	MainWindowThreadCleanup(mWin->hwnd);

#ifndef _MGNCS_INCORE_RES
	ncsUnloadResPackage(hPackage);
#endif

	ncsUninitialize();
#endif

	return 0;
}
