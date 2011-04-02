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


int DlModuleInit(void)
{
#ifdef ntStartWindowEx
	MSG Msg;
	mMainWnd* mWin;

#ifdef _MGNCS_INCORE_RES
	ncsSetAppIniInfo(ncsGetIncoreAppIniInfo());

	ncsInitialize();

	hPackage = ncsLoadIncoreResPackage();
#else
	char res_path [MAX_PATH];
	char f_package[MAX_PATH];

	ncsSetAppIniInfo(ncsLoadAppIniInfo((const char *)basename((char *)argv[0])));

	ncsInitialize();

	if (ETC_OK != ncsGetValueFromIniInfo (PATH_INFO_SECT, RES_PATH_KEY,
				res_path, MAX_PATH)) {
		fprintf(stderr, "Resource Path not found.\n");
		return 1;
	}
	SetResPath(res_path);

	if (!ncsGetResPackage("$(projectName).res", f_package, MAX_PATH)) {
		fprintf(stderr, "Haven't find the  Resource Package File.");
		return 1;
	}

	hPackage = ncsLoadResPackage(f_package);
#endif

	if (hPackage == HPACKAGE_NULL) {
#ifdef _MGNCS_INCORE_RES
		fprintf(stderr, "Error: load in-core resource package failure.\n");
#else
		fprintf(stderr, "Error: load resource package:%s failure.\n", f_package);
#endif
		return 1;
	}

	SetDefaultWindowElementRenderer(ncsGetString(hPackage, NCSRM_SYSSTR_DEFRDR));
#ifdef _MGRM_PROCESSES
	JoinLayer(NAME_DEF_LAYER, "$(projectName)", 0, 0);
#endif

	mWin = ntStartWindowEx(hPackage, HWND_DESKTOP, (HICON)0, (HMENU)0, (DWORD)0);

	while (GetMessage(&Msg, mWin->hwnd)) {
		TranslateMessage(&Msg);
		DispatchMessage(&Msg);
	}

	ncsUnloadResPackage(hPackage);

	ncsUninitialize();
	ncsUnloadAppIniInfo();
#endif

	return 0;
}

void DlModuleDeinit(void)
{
}

