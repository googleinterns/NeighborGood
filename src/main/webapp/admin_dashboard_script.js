/**
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

google.charts.load('current', {'packages':['line']});
google.charts.setOnLoadCallback(drawChart);

window.addEventListener("resize", drawChart);

function drawChart() {
	var data = new google.visualization.DataTable();
	data.addColumn('number', 'Days');
	data.addColumn('number', 'Admins');
	data.addColumn('number', 'Consecutive Login');
	data.addColumn('number', 'Tasks');
	data.addRows([
		[1,  37.8, 80.8, 41.8],
		[2,  30.9, 69.5, 32.4],
		[3,  25.4,   57, 25.7],
		[4,  11.7, 18.8, 10.5],
		[5,  11.9, 17.6, 10.4],
		[6,   8.8, 13.6,  7.7],
		[7,   7.6, 12.3,  9.6],
		[8,  12.3, 29.2, 10.6],
		[9,  16.9, 42.9, 14.8],
		[10, 12.8, 30.9, 11.6],
		[11,  5.3,  7.9,  4.7],
		[12,  6.6,  8.4,  5.2],
		[13,  4.8,  6.3,  3.6],
		[14,  4.2,  6.2,  3.4]
	]);
	var chart = new google.charts.Line(document.getElementById('chart-div'));
	chart.draw(data);
}
