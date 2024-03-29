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

#include <minigui/common.h>

#ifdef _MGIAL_DLCUSTOM
#include <minigui/minigui.h>
#include <minigui/gdi.h>
#include <minigui/window.h>
#include <minigui/customial.h>


static int mouse_x;
static int mouse_y;
static int mouse_button;

typedef struct tagPOS {
	short x;
	short y;
	short b;
} POS;


static int mouse_update (void)
{
	return 1;
}

static void mouse_getxy (int* x, int* y)
{
	*x = mouse_x;
	*y = mouse_y;
}

static int mouse_getbutton (void)
{
	return 0;
}

static int keyboard_update (void)
{
	return 0;
}

static const char* keyboard_getstate (void)
{
	return NULL;
}

static int wait_event (int which, int maxfd, fd_set* in, fd_set* out,
		fd_set* except, struct timeval* timeout)
{
#ifdef _MGRM_THREADS
	__mg_os_time_delay(300);
#else
	fd_set rfds;
	int e;

	if (!in) {
		in = &rfds;
		FD_ZERO(in);
	}

	e = select(maxfd + 1, in, out, except, timeout);

	if (e < 0) {
		return -1;
	}
#endif

	return 0;
}

BOOL InitCustomInput (INPUT* input, const char* mdev, const char* mtype)
{
	input->update_mouse = mouse_update;
	input->get_mouse_xy = mouse_getxy;
	input->set_mouse_xy = NULL;
	input->get_mouse_button = mouse_getbutton;
	input->set_mouse_range = NULL;
	input->suspend_mouse= NULL;
	input->resume_mouse = NULL;

	input->update_keyboard = keyboard_update;
	input->get_keyboard_state = keyboard_getstate;
	input->suspend_keyboard = NULL;
	input->resume_keyboard = NULL;
	input->set_leds = NULL;

	input->wait_event = wait_event;

	mouse_x = 0;
	mouse_y = 0;
	mouse_button = 0;

	return TRUE;
}

void TermCustomInput (void)
{
}

#endif /*  _MGIAL_DLCUSTOM */

