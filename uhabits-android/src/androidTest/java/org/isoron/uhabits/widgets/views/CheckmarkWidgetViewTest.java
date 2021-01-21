/*
 * Copyright (C) 2016-2021 Álinson Santos Xavier <git@axavier.org>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.uhabits.widgets.views;

import androidx.test.ext.junit.runners.*;
import androidx.test.filters.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.core.models.*;
import org.isoron.uhabits.core.utils.*;
import org.isoron.uhabits.utils.*;
import org.junit.*;
import org.junit.runner.*;

import java.io.*;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class CheckmarkWidgetViewTest extends BaseViewTest
{
    private static final String PATH = "widgets/CheckmarkWidgetView/";

    private CheckmarkWidgetView view;

    @Override
    @Before
    public void setUp()
    {
        super.setUp();
        setTheme(R.style.WidgetTheme);

        Habit habit = fixtures.createShortHabit();
        Timestamp today = DateUtils.getTodayWithOffset();

        view = new CheckmarkWidgetView(targetContext);
        double score = habit.getScores().get(today).getValue();
        float percentage = (float) score;

        view.setActiveColor(PaletteUtils.getAndroidTestColor(0));
        view.setEntryState(habit.getComputedEntries().get(today).getValue());
        view.setEntryValue(habit.getComputedEntries().get(today).getValue());
        view.setPercentage(percentage);
        view.setName(habit.getName());
        view.refresh();
        measureView(view, dpToPixels(100), dpToPixels(200));
    }

    @Test
    public void testRender_checked() throws IOException
    {
        assertRenders(view, PATH + "checked.png");
    }


    @Test
    public void testRender_largeSize() throws IOException
    {
        measureView(view, dpToPixels(300), dpToPixels(300));
        assertRenders(view, PATH + "large_size.png");
    }

}
