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
#include <string.h>
#include <libgen.h>

#include <minigui/common.h>
#include <minigui/minigui.h>
#include <minigui/gdi.h>
#include <minigui/window.h>

#include <mgncs/mgncs.h>

#include "resource.h"
#include "ncs-windows.h"

HPACKAGE hPackage = HPACKAGE_NULL;

#ifdef _MGNCS_INCORE_RES
extern HPACKAGE ncsLoadIncoreResPackage(void);
extern GHANDLE ncsGetIncoreEtc(void);
extern GHANDLE ncsGetIncoreAppIniInfo(void);
#endif

int MiniGUIMain(int argc, const char* argv[])
{
#ifdef ntStartWindowEx
	MSG Msg;
	mMainWnd *mWin;

#ifdef _MGNCS_INCORE_RES
	ncsSetAppIniInfo(ncsGetIncoreAppIniInfo());

	ncsInitialize();

	hPackage = ncsLoadIncoreResPackage();
#else
	char res_path [MAX_PATH];
	char f_package[MAX_PATH];

	ncsSetAppIniInfo(ncsLoadAppIniInfo((const char *)basename((char *)argv[0])));

	ncsInitialize();

	if (ETC_OK != ncsGetValueFromIniInfo (PATH_INFO_SECT,
				RES_PATH_KEY, res_path, MAX_PATH)){
		fprintf(stderr, "Resource Path not found.\n");
		return 1;
	}
	SetResPath(res_path);

	if (!ncsGetResPackage("$(projectName).res", f_package, MAX_PATH)) {
		fprintf(stderr, "Haven't find the  Resource Package File.");
		return 1;
	}

	hPackage = ncsLoadResPackage (f_package);
#endif

	if (hPackage == HPACKAGE_NULL) {
#ifdef _MGNCS_INCORE_RES
		fprintf (stderr, "Error: load in-core resource package failure.\n");
#else
		fprintf (stderr, "Error: load resource package:%s failure.\n", f_package);
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

	ncsUnloadResPackage(hPackage);

	ncsUninitialize();
	ncsUnloadAppIniInfo();
#endif

	return 0;
}
