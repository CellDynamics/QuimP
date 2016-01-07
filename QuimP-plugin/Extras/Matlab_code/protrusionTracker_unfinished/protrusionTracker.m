function varargout = protrusionTracker(varargin)
% PROTRUSIONTRACKER MATLAB code for protrusionTracker.fig
%      PROTRUSIONTRACKER, by itself, creates a new PROTRUSIONTRACKER or raises the existing
%      singleton*.
%
%      H = PROTRUSIONTRACKER returns the handle to a new PROTRUSIONTRACKER or the handle to
%      the existing singleton*.
%
%      PROTRUSIONTRACKER('CALLBACK',hObject,eventData,handles,...) calls the local
%      function named CALLBACK in PROTRUSIONTRACKER.M with the given input arguments.
%
%      PROTRUSIONTRACKER('Property','Value',...) creates a new PROTRUSIONTRACKER or raises the
%      existing singleton*.  Starting from the left, property value pairs are
%      applied to the GUI before protrusionTracker_OpeningFcn gets called.  An
%      unrecognized property name or invalid value makes property application
%      stop.  All inputs are passed to protrusionTracker_OpeningFcn via varargin.
%
%      *See GUI Options on GUIDE's Tools menu.  Choose "GUI allows only one
%      instance to run (singleton)".
%
% See also: GUIDE, GUIDATA, GUIHANDLES

% Edit the above text to modify the response to help protrusionTracker

% Last Modified by GUIDE v2.5 19-Feb-2014 10:17:16

    % Begin initialization code - DO NOT EDIT
    gui_Singleton = 1;
    gui_State = struct('gui_Name',       mfilename, ...
                   'gui_Singleton',  gui_Singleton, ...
                   'gui_OpeningFcn', @protrusionTracker_OpeningFcn, ...
                   'gui_OutputFcn',  @protrusionTracker_OutputFcn, ...
                   'gui_LayoutFcn',  [] , ...
                   'gui_Callback',   []);
    if nargin && ischar(varargin{1})
        gui_State.gui_Callback = str2func(varargin{1});
    end

    if nargout
        [varargout{1:nargout}] = gui_mainfcn(gui_State, varargin{:});
    else
        gui_mainfcn(gui_State, varargin{:});
    end
% End initialization code - DO NOT EDIT
end


% --- Executes just before protrusionTracker is made visible.
function protrusionTracker_OpeningFcn(hObject, eventdata, handles, varargin)
% This function has no output args, see OutputFcn.
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
% varargin   command line arguments to protrusionTracker (see VARARGIN)

    % Choose default command line output for protrusionTracker
    handles.output = hObject;

    % Update handles structure
    guidata(hObject, handles);

    % UIWAIT makes protrusionTracker wait for user response (see UIRESUME)
    % uiwait(handles.fig_protrusionTracker);
end

% --- Outputs from this function are returned to the command line.
function varargout = protrusionTracker_OutputFcn(hObject, eventdata, handles) 
% varargout  cell array for returning output args (see VARARGOUT);
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Get default command line output from handles structure
varargout{1} = handles.output;
end

% --- Executes on slider movement.
function slider_frame_Callback(hObject, eventdata, handles)
% hObject    handle to slider_frame (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'Value') returns position of slider
%        get(hObject,'Min') and get(hObject,'Max') to determine range of slider
    sliderPos = floor(get(handles.slider_frame,'Value')+0.5);
    handles = moveToFrame(handles, sliderPos);
    guidata(hObject,handles);


    
end

function handles = moveToFrame(handles, t)
    if( handles.loadedData ~= 1), return; end % no data loaded
    
    
    %sliderMin = get(hObject,'Min');
    %sliderMax = get(hObject,'Max');
    %sliderStep = get(hObject,'SliderStep');
    
    set(handles.slider_frame,'Value', t); % round it to frames
    handles.frame = t - (handles.curCell.startFrame-1);
    handles.absFrame = t;
   
    %set text_frame
    s = sprintf('Frame: %04d (%04.2f sec)',handles.absFrame, handles.curCell.FI*handles.absFrame ); 
    set(handles.text_frame, 'String', s);
    
    % set map objects
    set(gcf, 'currentaxes', handles.axis_motility);
    delete(handles.hLine_mapFrame);
    handles.hLine_mapFrame = line( [0,handles.mapSize(2)],[handles.frame ,handles.frame ],'Color', [0.6,0.6,0.6]);

    set(handles.hLine_mapFrame, 'XData',[0,handles.mapSize(2)])
    set(handles.hLine_mapFrame, 'YData',[handles.frame ,handles.frame ])

    
    plot_Image(handles.image_axis,handles.curCell.outlines, handles.frame, handles.fov);
     

end

% --- Executes during object creation, after setting all properties.
function slider_frame_CreateFcn(hObject, eventdata, handles)
% hObject    handle to slider_frame (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: slider controls usually have a light gray background.
if isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor',[.9 .9 .9]);
end
end

% --- Executes on selection change in cell_list.
function cell_list_Callback(hObject, eventdata, handles)
% hObject    handle to cell_list (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: contents = cellstr(get(hObject,'String')) returns cell_list contents as cell array
%        contents{get(hObject,'Value')} returns selected item from cell_list

    if( handles.loadedData ~= 1),
        return;
    end % no data loaded
    
    selected = get(hObject,'Value');
    
    % set up for cell selected at frame 1
    handles = setupForCell(handles, selected);
    
    % update handles and draw
    guidata(hObject,handles);
    draw_Frame(handles.image_axis,handles.curCell.outlines, handles.frame, handles.fov);


end

% --- Executes during object creation, after setting all properties.
function cell_list_CreateFcn(hObject, eventdata, handles)
% hObject    handle to cell_list (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: popupmenu controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end

end


% --- Executes on button press in load_button.
function load_button_Callback(hObject, eventdata, handles)
% hObject    handle to load_button (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
    
   %[FileName,PathName] = uigetfile('*.mat','Select the MATLAB data file')
   FileName='qCells.mat';
   PathName='/Users/rtyson/Documents/MATLAB/QuimP11_MATLAB/protrusionTracker/';
   
   S = load([PathName, FileName]);
   SNames = fieldnames(S); 
   handles.qCells = S.(SNames{1});
   handles.nCells = length(handles.qCells);
   handles.loadedData = 1;  

   % populate cell list dropdown
   list = cell(handles.nCells , 1);
   for i = 1:handles.nCells,
       list{i} = handles.qCells(i).name;
   end
   set(handles.cell_list,'String', list);
   set(handles.cell_list,'Value', 1); % cell 1

   %set image axis and plot properties
   set(gcf, 'currentaxes', handles.image_axis);
   hold on
   axis manual
   axis equal
   handles.fov = 4; % number of frames to plot in the future
   handles.hLine_mapFrame = []; % handle,line drawn across mot map at current frame
   
   % set up output data
   handles.qProts = newQProtData(handles.qCells);
   
   % set up for cell 1 at frame 1
   handles = setupForCell(handles, 1);
   
   % update handles and draw
   handles.hLine_mapFrame = line( [0,handles.mapSize(2)],[1,1],'Color', [0.6,0.6,0.6]); % first plot of line
   plotDisMap(handles.axis_motility, handles.curProt.disMap, handles.curCell.FI);

   handles = moveToFrame(handles, handles.frame);

   handles
   guidata(hObject,handles);

end


% --- Executes on button press in load_im_seq.
function load_im_seq_Callback(hObject, eventdata, handles)
% hObject    handle to load_im_seq (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
    
end

function handles = setupForCell(handles, cn)
    %write changed cell back into qCells

   handles.curCell = handles.qCells(cn);
   handles.curProt = handles.qProts(cn);
   handles.frame = 1;
   handles.absFrame = handles.curCell.startFrame;
   handles.nbFrames = handles.curCell.nbFrames;
   
   handles.marker = [0,0];  % t, position
   
   % ---------set frame axis slider properties----------
   set(handles.slider_frame, 'Min', handles.curCell.startFrame);
   set(handles.slider_frame, 'Max', handles.curCell.endFrame);
   set(handles.slider_frame, 'Value', handles.curCell.startFrame);
   set(handles.slider_frame, 'SliderStep', [1/handles.curCell.nbFrames, 0.1]);
   %set text_frame
   s = sprintf('Frame: %04d (%04.2f sec)',handles.absFrame, handles.curCell.FI*handles.absFrame ); 
   set(handles.text_frame, 'String', s);

   % ----------set dismap options---------
   set(handles.slider_disWindowTime, 'Min', 0);
   set(handles.slider_disWindowTime, 'Max', handles.curCell.nbFrames.*handles.curCell.FI);
   set(handles.slider_disWindowTime, 'Value',0);
   set(handles.slider_disWindowTime, 'SliderStep', [1/(handles.curCell.nbFrames.*handles.curCell.FI), 0.1]);
   
   set(handles.slider_disWindowWidth, 'Min', 0);
   set(handles.slider_disWindowWidth, 'Max', 40);
   set(handles.slider_disWindowWidth, 'Value',0);
   set(handles.slider_disWindowWidth, 'SliderStep', [1/40, 0.1]);
   %set text_frame
   s = sprintf('%.2f sec', 0); 
   set(handles.text_disTimeValue, 'String', s);
   s = sprintf('%.2f um', 0); 
   set(handles.text_disWidthValue, 'String', s);
   
   set(handles.button_updateDis,'Enable','on');
   
   %----set peak finding options--------
   s = sprintf('%.2f um', 0); 
   setupSlider(handles.slider_peakThresh,handles.text_peakThreshValue, 0, 3, 0, 1/60, s); 
   setupSlider(handles.slider_meanThresh,handles.text_meanThreshValue, 0, 3, 0, 1/60, s);
   set( handles.checkbox_showPeakMap, 'Value', 0);
   set(handles.checkbox_showPeakMap,'Enable','off');
      
   set(gcf, 'currentaxes', handles.image_axis);
   xlim(handles.curCell.R(1:2)+[-5,5]);
   ylim(handles.curCell.R(3:4)+[-5,5]);
   
   if( isempty(handles.curProt.disMap)),      
       handles.curProt.disMap = handles.curCell.motilityMap;
   end
   
   handles.mapSize = size(handles.curProt.disMap);


end

function setupSlider(hSlider,hText, min, max, value, step, s)
   set(hSlider, 'Min', min);
   set(hSlider, 'Max', max);
   set(hSlider, 'Value',value);
   set(hSlider, 'SliderStep', [step, 0.1]);
   set(hText, 'String', s);
end

function plot_Image(hAxis,outlines, frame, fov)
    set(gcf, 'currentaxes', hAxis);
    cla(hAxis);
 
    if((fov+frame)>size(outlines,1)),
        fov = size(outlines,1) - frame;
    end
    
    colStep = 0.3/fov;
    c = [0.9,0.9,0.9];
    for i = fov:-1:1,
       
       plot_outline(outlines{frame+i}(:,2:3), c, 1);  
       c= c - colStep;
    end

    plot_outline(outlines{frame}(:,2:3), 'k', 2);

end

function plot_outline( c1, col, w)

  plot([c1(1,1), c1(end,1)],[c1(1,2), c1(end,2)],'-','color',col,'MarkerFaceColor',col,'LineWidth',w);
  plot(c1(:,1), c1(:,2),'-', 'color', col ,'MarkerFaceColor',col,'LineWidth',w);

end

function plotDisMap(hAxis, map, FI)

    set(gcf, 'currentaxes', hAxis);
    cla(hAxis);

    Ulim = max(max(abs(map)));
    Llim = -Ulim;
    
    cMapRes = 512; % colour resolution

    cMap = jet(cMapRes); % colors
    ytickspaceframes = round(size(map,1)/10);    
    cLim = getNewClim(1,cMapRes, Llim, Ulim,length(cMap));

    IM = image( map , 'Parent', hAxis);
    
    xlim=get(hAxis, 'XLim');
    xticks = xlim(1):100:xlim(2);
    set(hAxis, 'XTick', xticks);
    set(hAxis, 'XTickLabel', ( 0:1/(length(xticks)-1):1 ) ); 
    
    ylim=get(hAxis, 'YLim');
    yticks = ylim(1):ytickspaceframes:ylim(2);
    set(hAxis, 'YTick', yticks);
    
    yLabels = 0:(FI*size(map,1))/(length(yticks)-1):(FI*size(map,1)) ;
    yLabels = round(yLabels.*ytickspaceframes)/ytickspaceframes;
    set(hAxis, 'YTickLabel', yLabels);
    
    colormap(cMap);
    set(IM,'CDataMapping', 'scaled');
    caxis(cLim);
    drawnow
end

function CLim = getNewClim(BeginSlot,EndSlot,CDmin,CDmax,CmLength)
   % 				Convert slot number and range
   % 				to percent of colormap
   PBeginSlot    = (BeginSlot - 1) / (CmLength - 1);
   PEndSlot      = (EndSlot - 1) / (CmLength - 1);
   PCmRange      = PEndSlot - PBeginSlot;
   % 				Determine range and min and max 
   % 				of new CLim values
   DataRange     = CDmax - CDmin;
   ClimRange     = DataRange / PCmRange;
   NewCmin       = CDmin - (PBeginSlot * ClimRange);
   NewCmax       = CDmax + (1 - PEndSlot) * ClimRange;
   CLim          = [NewCmin,NewCmax];
end

function plotPeakMap(hAxis, map, FI)

    set(gcf, 'currentaxes', hAxis);
    cla(hAxis);
    
    cMapRes = 512; % colour resolution

    cMap = jet(cMapRes); % colors
    ytickspaceframes = round(size(map,1)/10);    
   

    IM = imagesc( map , 'Parent', hAxis);
    
    xlim=get(hAxis, 'XLim');
    xticks = xlim(1):100:xlim(2);
    set(hAxis, 'XTick', xticks);
    set(hAxis, 'XTickLabel', ( 0:1/(length(xticks)-1):1 ) ); 
    
    ylim=get(hAxis, 'YLim');
    yticks = ylim(1):ytickspaceframes:ylim(2);
    set(hAxis, 'YTick', yticks);
    
    yLabels = 0:(FI*size(map,1))/(length(yticks)-1):(FI*size(map,1)) ;
    yLabels = round(yLabels.*ytickspaceframes)/ytickspaceframes;
    set(hAxis, 'YTickLabel', yLabels);
    
    colormap(cMap);
    set(IM,'CDataMapping', 'scaled');
   
    drawnow
end

function qProts = newQProtData( qCells)

   for i = 1:length(qCells),
     qProts(i).index = qCells(i).index;  
     qProts(i).name = qCells(i).name; 
     
     qProts(i).disWindow = [0,0]; % width, time (microns,seconds)
     qProts(i).disMap = [];
     
     qProts(i).peakMap = [];
     qProts(i).peakThresh = 0;
     qProts(i).meanThresh = 0;
     
     qProts(i).nProts = 0;
     qProts(i).prots = cell(0,0);
   end

end


% --- Executes on slider movement.
function slider_disWindowWidth_Callback(hObject, eventdata, handles)
    sliderPos = get(hObject,'Value');
    handles.curProt.disWindow(1) = sliderPos;
    %set text_frame
    s = sprintf('%.2f um',sliderPos); 
    set(handles.text_disWidthValue, 'String', s);
    guidata(hObject,handles);
end

% --- Executes during object creation, after setting all properties.
function slider_disWindowWidth_CreateFcn(hObject, eventdata, handles)
% hObject    handle to slider_disWindowWidth (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: slider controls usually have a light gray background.
    if isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
        set(hObject,'BackgroundColor',[.9 .9 .9]);
    end
end

% --- Executes on slider movement.
function slider_disWindowTime_Callback(hObject, eventdata, handles)
    sliderPos = get(hObject,'Value');
    handles.curProt.disWindow(2) = sliderPos;
    %set text_frame
    s = sprintf('%.2f sec',sliderPos); 
    set(handles.text_disTimeValue, 'String', s);
    guidata(hObject,handles);
end

% --- Executes during object creation, after setting all properties.
function slider_disWindowTime_CreateFcn(hObject, eventdata, handles)
% hObject    handle to slider_disWindowTime (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: slider controls usually have a light gray background.
    if isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
        set(hObject,'BackgroundColor',[.9 .9 .9]);
    end
end

% --- Executes on button press in button_updateDis.
function button_updateDis_Callback(hObject, eventdata, handles)
   s = sprintf('computing'); 
   set(handles.button_updateDis, 'String', s);
   set(hObject,'Enable','off');
   drawnow
   
   handles.curProt.disMap = buildDisMap( handles.curCell, handles.curProt.disWindow);
   plotDisMap(handles.axis_motility, handles.curProt.disMap, handles.curCell.FI);
   handles.hLine_mapFrame = line( [0,handles.mapSize(2)],[handles.frame,handles.frame],'Color', [0.6,0.6,0.6]);

   s = sprintf('update'); 
   set(handles.button_updateDis, 'String', s);
   set(hObject,'Enable','on');
   drawnow
   
   set(handles.checkbox_showPeakMap, 'Value', 1);
   
   guidata(hObject,handles);
end


% --- Executes on slider movement.
function slider_peakThresh_Callback(hObject, eventdata, handles)
    sliderPos = get(hObject,'Value');
    handles.curProt.peakThresh = sliderPos;
    %set text_frame
    s = sprintf('%.2f um',sliderPos); 
    set(handles.text_peakThreshValue, 'String', s);
    guidata(hObject,handles);
end

% --- Executes during object creation, after setting all properties.
function slider_peakThresh_CreateFcn(hObject, eventdata, handles)
    if isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
      set(hObject,'BackgroundColor',[.9 .9 .9]);
    end
end

% --- Executes on button press in checkbox_showPeakMap.
function checkbox_showPeakMap_Callback(hObject, eventdata, handles)
    % Hint: get(hObject,'Value') returns toggle state of checkbox_showPeakMap

    if( isempty(handles.curProt.peakMap)),
        fprintf('No peakMap computed yet!. Hit update \n');
        set( hObject, 'Value', 0);
        return;
    end
    
    if( get(hObject,'Value') ),
       %is checked on. plot peak map
       plotPeakMap(handles.axis_motility, handles.curProt.peakMap, handles.curCell.FI);
       handles.hLine_mapFrame = line( [0,handles.mapSize(2)],[handles.frame,handles.frame],'Color', [0.6,0.6,0.6]);
    else
       %is check off. plot dis map 
       plotDisMap(handles.axis_motility, handles.curProt.disMap, handles.curCell.FI);
       handles.hLine_mapFrame = line( [0,handles.mapSize(2)],[handles.frame,handles.frame],'Color', [0.6,0.6,0.6]);
    end
    
    guidata(hObject,handles);
end


% --- Executes on slider movement.
function slider_meanThresh_Callback(hObject, eventdata, handles)
    sliderPos = get(hObject,'Value');
    handles.curProt.meanThresh = sliderPos;
    %set text_frame
    s = sprintf('%.2f um',sliderPos); 
    set(handles.text_meanThreshValue, 'String', s);
    guidata(hObject,handles);
end

% --- Executes during object creation, after setting all properties.
function slider_meanThresh_CreateFcn(hObject, eventdata, handles)
% hObject    handle to slider_meanThresh (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: slider controls usually have a light gray background.
if isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor',[.9 .9 .9]);
end
end


% --- Executes on button press in button_updatePeaks.
function button_updatePeaks_Callback(hObject, eventdata, handles)
% hObject    handle to button_updatePeaks (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

   s = sprintf('computing'); 
   set(handles.button_updatePeaks, 'String', s);
   set(hObject,'Enable','off');
   drawnow
   
   handles.curProt.peakMap = buildPeakMap( handles.curProt.disMap, handles.curProt.peakThresh);
   plotPeakMap(handles.axis_motility, handles.curProt.peakMap, handles.curCell.FI);
   handles.hLine_mapFrame = line( [0,handles.mapSize(2)],[handles.frame,handles.frame],'Color', [0.6,0.6,0.6]);

   s = sprintf('update'); 
   set(handles.button_updatePeaks, 'String', s);
   set(hObject,'Enable','on');

   set(handles.checkbox_showPeakMap,'Enable','on');
   set(handles.checkbox_showPeakMap, 'Value', 1);
   
   drawnow
   guidata(hObject,handles);
end


% --- Executes on mouse press over figure background, over a disabled or
% --- inactive control, or over an axes background.
function fig_protrusionTracker_WindowButtonDownFcn(hObject, eventdata, handles)
% hObject    handle to fig_protrusionTracker (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
    
    clickMotility = get(handles.axis_motility,'CurrentPoint');
    clicked = checkInAxis(handles.axis_motility, clickMotility(1:4));
    if(clicked),
        %move to frame
        t = floor(clickMotility(3)+0.5); % time  
        t(t<1) = 1; t(t>handles.nbFrames) = handles.nbFrames; % bound
        handles = moveToFrame(handles, t);
        guidata(hObject,handles);
    end
    
end

function inBounds=checkInAxis(hAxis, C)
% check if CurrentPoint C is within axis bounds
    R = [get(hAxis,'xlim'),get(hAxis,'ylim')];  
    b=(C - R).*[1,-1,1,-1]; %if all +ve then within bounds
    inBounds = sum(b <= 0)==0;
end
