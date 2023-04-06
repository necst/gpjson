import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from matplotlib.patches import Patch
from plot_utils import *


sns.set_style("whitegrid", {"ytick.left": True})
plt.rcParams["font.family"] = ["serif"]
plt.rcParams["font.size"] = 12
plt.rcParams['hatch.linewidth'] = 0.6
plt.rcParams['axes.labelpad'] = 5 
plt.rcParams['pdf.fonttype'] = 42
plt.rcParams['ps.fonttype'] = 42

colors = sns.color_palette('husl', n_colors=11)
dir = "/Users/lucadanelutti/repo/gpjson/benchmarks/result/thesis/"

def GPUsBatch1(filename):
    frame = pd.DataFrame()
    temp = pd.read_csv(dir+"batch1/"+filename)
    temp['machine'] = filename[:-4].upper()
    frame = pd.concat([frame, temp])
    frame['time'] = frame['time'] / 1000
    frame['stddev'] = frame['stddev'] / 1000
    tempColors = [colors[i] for i in [3, 4, 5, 6, 7, 8, 9, 10]]
    tempColors.insert(0, colors[0])
    return {"name": "GPUOverview-"+filename,
        "data": frame,
        "ratio": "gpjson", 
        "bar_label": "edge",
        "bar_label_padding": 3,
        "ncols": 5,
        "bbox_to_anchor": 0.8,
        "bottomPadding": 0.15,
        "topPadding": 0.9,
        "xlabel": "Engine", 
        "ylabel": "Execution Time [s]",
        "engine_order": ['gpjson', 'nodejsonpath', 'nodejsonpathplus', 'nodemanual', 'nodesimdjson', 'javajsonpath', 'pison', 'rapidjson', 'simdjson'],
        "col": 'query',
        "col_order": ['TT1', 'TT2', "TT3", "TT4", "WM", "BB"],
        "col_labels": ['TT1', 'TT2', "TT3", "TT4", "WM", "BB"],
        "labels": ['GpJSON', 'Node jsonpath', 'Node jsonpath-plus', 'Node manual', 'Node simdjson', 'Java JSONPath', 'Pison', 'RapidJSON', 'simdjson'],
        "colors": tempColors
        }

def HPCBatch1():
    frame = pd.DataFrame()
    for filename in ['gpu4.8.csv', 'gpu3.1.csv', 'gpu2.1.csv']:
        temp = pd.read_csv(dir+"batch1/"+filename)
        temp = temp.loc[temp['engine'] == "gpjson"]
        temp['machine'] = "OPTIMIZED3-12c"
        temp['engine'] = "gpjson" + "-" + filename[:-4].upper()
        frame = pd.concat([frame, temp])
    temp = pd.read_csv(dir+"batch1/optimized3-12c.csv")
    temp['machine'] = "OPTIMIZED3-12c"
    frame = pd.concat([frame, temp])
    frame['time'] = frame['time'] / 1000
    frame['stddev'] = frame['stddev'] / 1000
    return {"name": "HPCBatch1",
        "data": frame,
        "ratio": "gpjson-GPU4.8", 
        "bar_label": "edge",
        "bar_label_padding": 3, 
        # "limit": [[0, 8],[0, 10],[0, 10],[0, 10],[0, 10],[0, 20]],   
        "xlabel": "Engine", 
        "ylabel": "Execution Time [s]",
        "ncols": 4,
        "bbox_to_anchor": 0.6,
        "bottomPadding": 0.18,
        "topPadding": 0.9,
        "engine_order": ['gpjson-GPU4.8', 'gpjson-GPU3.1', 'gpjson-GPU2.1', 'nodejsonpath', 'nodejsonpathplus', 'nodemanual', 'nodesimdjson', 'javajsonpath', 'pison', 'rapidjson', 'simdjson'],
        "col": 'query',
        "col_order": ['TT1', 'TT2', "TT3", "TT4", "WM", "BB"],
        "col_labels": ['TT1', 'TT2', "TT3", "TT4", "WM", "BB"],
        "col_wrap": 3,
        "labels": ['GpJSON-GPU4.8', 'GpJSON-GPU3.1', 'GpJSON-GPU2.1', 'Node jsonpath', 'Node jsonpath-plus', 'Node manual', 'Node simdjson', 'Java JSONPath', 'Pison', 'RapidJSON', 'simdjson'],
        "colors": colors
        }

def sizes():
    frame = pd.DataFrame()
    for filename in ['gpu4.8.csv', 'gpu3.1.csv', 'gpu2.1.csv']:
        temp = pd.read_csv(dir+"sizes/"+filename)
        temp = temp.loc[temp['engine'] == "gpjson"]
        temp['machine'] = "OPTIMIZED3-12c"
        temp['engine'] = "gpjson" + "-" + filename[:-4].upper()
        frame = pd.concat([frame, temp])
    temp = pd.read_csv(dir+"sizes/optimized3-12c.csv")
    temp['machine'] = "OPTIMIZED3-12c"
    frame = pd.concat([frame, temp])
    frame['stddev'] = 0
    frame['time'] = frame['time'] / 1000
    frame['dataset'] = frame['dataset'].apply(lambda x: x.split("/")[-1])
    baseSize = 0.807
    for engine in frame['engine'].unique():
        for machine in frame['machine'].unique():
            for dataset in ['twitter_small_records.json', 'twitter_small_records_0.125x.json', 'twitter_small_records_0.25x.json', 'twitter_small_records_0.5x.json', 'twitter_small_records_2x.json', 'twitter_small_records_4x.json', 'twitter_small_records_8x.json', 'twitter_small_records_12x.json', 'twitter_small_records_16x.json']:
                multiplier = 1 if (dataset == 'twitter_small_records.json') else float(dataset.split("_")[3].split("x")[0])
                frame.loc[(frame['machine'] == machine) & (frame['engine'] == engine) & (frame['dataset'] == dataset), 'time'] = baseSize * multiplier / frame.loc[(frame['machine'] == machine) & (frame['engine'] == engine) & (frame['dataset'] == dataset), 'time'].iloc[0]
    return {"name": "HPCSizes",
        "data": frame,
        "ratio": "gpjson-GPU4.8", 
        "bar_label": "edge",
        "bar_label_padding": 10,
        "xlabel": "Engine", 
        "ylabel": "Speed [GB/s]",
        "limit": [[0, 4],[0, 4],[0, 4],[0, 4], [0, 4],[0, 4],[0, 4],[0, 4],[0, 4]],
        "ncols": 4,
        "bbox_to_anchor": 0.45,
        "bottomPadding": 0.10,
        "topPadding": 0.92,
        "row": 'machine',
        "row_order": ['OPTIMIZED3-12c'],
        "engine_order": ['gpjson-GPU4.8', 'gpjson-GPU3.1', 'gpjson-GPU2.1', 'javajsonpath', 'pison', 'rapidjson', 'simdjson'],
        "col": 'dataset',
        "col_order": ["twitter_small_records_0.125x.json", "twitter_small_records_0.25x.json", "twitter_small_records_0.5x.json", "twitter_small_records.json", "twitter_small_records_2x.json", "twitter_small_records_4x.json", "twitter_small_records_8x.json", "twitter_small_records_12x.json", "twitter_small_records_16x.json"],
        "col_labels": ["0.125x", "0.25x", "0.5x", "1x", "2x", "4x", "8x", "12x", "16x"],
        "labels": ['GpJSON-GPU4.8', 'GpJSON-GPU3.1', 'GpJSON-GPU2.1' ,'Java JSONPath', 'Pison', 'RapidJSON', 'simdjson'],
        "colors": colors
        }

def selectivity():
    frame = pd.DataFrame()
    for filename in ['gpu4.8.csv', 'gpu3.1.csv', 'gpu2.1.csv']:
        temp = pd.read_csv(dir+"selectivity/"+filename)
        temp = temp.loc[temp['engine'] == "gpjson"]
        temp['machine'] = "OPTIMIZED3-12c"
        temp['engine'] = "gpjson" + "-" + filename[:-4].upper()
        frame = pd.concat([frame, temp])
    temp = pd.read_csv(dir+"selectivity/optimized3-12c.csv")
    temp['machine'] = "OPTIMIZED3-12c"
    frame = pd.concat([frame, temp])
    for engine in frame['engine'].unique():
        for dataset in frame['dataset'].unique():
            temp = dataset.split("_")[3].split(".")[0]
            i = 0
            while (temp[len(temp) - 1 - i] >= '0' and temp[len(temp) - 1 - i] <= '9'):
                i += 1
            frame.loc[(frame['engine'] == engine) & (frame['dataset'] == dataset), 'edit'] = dataset.split("_")[3].split(".")[0][:-i]
            frame.loc[(frame['engine'] == engine) & (frame['dataset'] == dataset), 'percentage'] = dataset.split("_")[3].split(".")[0][-i:]
    frame['time'] = frame['time'] / 1000
    frame['stddev'] = frame['stddev'] / 1000
    return {"name": "HPCSelectivity",
        "data": frame,
        "ratio": "gpjson-GPU4.8", 
        "bar_label": "edge",
        "bar_label_padding": 10,
        "xlabel": "Engine", 
        "ylabel": "Execution Time [s]",
        "limit": [0, 7],
        "ncols": 6,
        "bbox_to_anchor": 0,
        "bottomPadding": 0.12,
        "topPadding": 0.95,
        "row": 'edit',
        "row_order": ['changeUserLang', 'deleteUser', 'deleteUserLang'],
        "row_labels": ['Change $.user.lang', 'Delete $.user key', 'Delete $.user.lang key'],
        "engine_order": ['gpjson-GPU4.8', 'gpjson-GPU3.1', 'gpjson-GPU2.1', 'nodejsonpath', 'nodejsonpathplus', 'nodemanual', 'nodesimdjson', 'javajsonpath', 'pison', 'rapidjson', 'simdjson'],
        "col": 'percentage',
        "col_order": ["0", "25", "50", "75", "100"],
        "col_labels": ["0%", "25%", "50%", "75%", "100%"],
        "labels": ['GpJSON-GPU4.8', 'GpJSON-GPU3.1', 'GpJSON-GPU2.1', 'Node jsonpath', 'Node jsonpath-plus', 'Node manual', 'Node simdjson', 'Java JSONPath', 'Pison', 'RapidJSON', 'simdjson'],
        "colors": colors
        }

def doPlot(plot):
    data = plot['data']
    g = sns.catplot(data=data, kind='bar', x="engine",
                    order=plot['engine_order'],
                    y="time", alpha=1, palette=plot['colors'],
                    col=plot['col'], col_order=plot['col_order'],
                    row=plot['row'], row_order=plot['row_order'],
                    height=4, aspect=1,
                    sharey=False, sharex=False, margin_titles=True)

    g.set_axis_labels(plot['xlabel'], plot['ylabel'])

    cmap = dict(zip(plot['labels'], plot['colors']))
    patches = [Patch(color=v, label=k) for k, v in cmap.items()]
    plt.legend(handles=patches, bbox_to_anchor=(plot['bbox_to_anchor'], -0.2), loc='best', borderaxespad=0, ncols=plot['ncols'])

    for i,axes in enumerate(g.axes):  
        for ii,ax in enumerate(axes):
            if 'limit' in plot:
                ax.set(ylim=plot['limit'])
            ax.set(xticklabels=[])
            tempdata = data.loc[(data[plot['row']] == plot["row_order"][i]) & (data[plot['col']] == plot['col_order'][ii]) & (data['engine'].isin(plot['engine_order']))]
            ax.errorbar(data=tempdata, x="engine", y="time", yerr="stddev", fmt="none", c="k")
            labels = []
            for e in plot['engine_order']:
                ratio = tempdata.loc[tempdata['engine'] == e]['time'].values[0] / tempdata.loc[tempdata['engine'] == plot['ratio']]['time'].values[0]
                if ratio != 1:
                    labels.append(f"{ratio:.1f}x")
                else:
                    labels.append("")
            ax.bar_label(ax.containers[0], labels=labels, label_type=plot['bar_label'], padding=plot['bar_label_padding'], rotation=90)
            ax.set_title(plot['col_labels'][ii])

    plt.tight_layout()
    plt.subplots_adjust(hspace=0.4, wspace=0.2, bottom=plot['bottomPadding'], top=plot['topPadding'])

    plt.savefig(f"{plot['name']}.pdf")
    plt.show()

def doPlotOneRow(plot):
    data = plot['data']
    g = sns.catplot(data=data, kind='bar', x="engine",
                    order=plot['engine_order'],
                    y="time", alpha=1, palette=plot['colors'],
                    col=plot['col'], col_order=plot['col_order'],
                    col_wrap=3,
                    height=4, aspect=1,
                    sharey=False, sharex=False, margin_titles=True)

    g.set_axis_labels(plot['xlabel'], plot['ylabel'])

    cmap = dict(zip(plot['labels'], plot['colors']))
    patches = [Patch(color=v, label=k) for k, v in cmap.items()]
    plt.legend(handles=patches, bbox_to_anchor=(plot['bbox_to_anchor'], -0.2), loc='best', borderaxespad=0, ncols=plot['ncols'])

    for i,a in enumerate(g.axes):  
        if 'limit' in plot:
            a.set(ylim=plot['limit'][i])
        a.set(xticklabels=[])
        tempdata = data.loc[(data[plot['col']] == plot['col_order'][i]) & (data['engine'].isin(plot['engine_order']))]
        a.errorbar(data=tempdata, x="engine", y="time", yerr="stddev", fmt="none", c="k")
        labels = []
        for e in plot['engine_order']:
            ratio = tempdata.loc[tempdata['engine'] == e]['time'].values[0] / tempdata.loc[tempdata['engine'] == plot['ratio']]['time'].values[0]
            if ratio != 1:
                labels.append(f"{ratio:.1f}x")
            else:
                labels.append("")
        a.bar_label(a.containers[0], labels=labels, label_type=plot['bar_label'], padding=plot['bar_label_padding'], rotation=90)
        a.set_title(plot['col_labels'][i], y=1.15)

    plt.tight_layout()
    plt.subplots_adjust(hspace=0.4, wspace=0.2, bottom=plot['bottomPadding'], top=plot['topPadding'])

    plt.savefig(f"{plot['name']}.pdf")
    plt.show()
    

for filename in ['gpu4.8.csv', 'gpu3.1.csv', 'gpu2.1.csv']:
    doPlotOneRow(GPUsBatch1(filename))

doPlotOneRow(HPCBatch1())

doPlotOneRow(sizes())

doPlot(selectivity())