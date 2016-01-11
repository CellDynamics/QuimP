function varargout = shapeFilteringTest(varargin)
% SHAPEFILTERINGTEST MATLAB code for shapeFilteringTest.fig
%     External functions for processing shapes are connexted through
%     handles updated on every click at radiobuttons or their parameters.
%     In every radio button and on the shapeFilteringTest_OpeningFcn algorithms
%     parameters are switched off to prevent desynchronisation. 
%     To add new function follow existing processing methods and code of
%     radiobuttons and editboxes with parameters. Take a look on starting
%     method as well. 
%     Whole processing occurs in updateImage method.
%
% Author:   Piotr Baniukiewicz
% mail:     p.baniukiewicz@warwick.ac.uk
% Date:     08 Jan 2016
%
% Last Modified by GUIDE v2.5 11-Jan-2016 11:38:00

% Begin initialization code - DO NOT EDIT
gui_Singleton = 1;
gui_State = struct('gui_Name',       mfilename, ...
                   'gui_Singleton',  gui_Singleton, ...
                   'gui_OpeningFcn', @shapeFilteringTest_OpeningFcn, ...
                   'gui_OutputFcn',  @shapeFilteringTest_OutputFcn, ...
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


% --- Executes just before shapeFilteringTest is made visible.
function shapeFilteringTest_OpeningFcn(hObject, eventdata, handles, varargin)
% This function has no output args, see OutputFcn.
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
% varargin   command line arguments to shapeFilteringTest (see VARARGIN)

% Choose default command line output for shapeFilteringTest
handles.output = hObject;

handles.qCells = [];
handles.isOutline = 0;
handles.isOutlineMod = 0;
set(handles.fcnMedianParam,'Enable','off');
set(handles.fcnMeanParam,'Enable','off');
set(handles.fcnMMParam,'Enable','off');
set(handles.fcnDPParam,'Enable','off');
set(handles.fcnHampelParam,'Enable','off');
set(handles.fcnHampelMeanParam,'Enable','off');
% Update handles structure
guidata(hObject, handles);

% UIWAIT makes shapeFilteringTest wait for user response (see UIRESUME)
% uiwait(handles.figure1);


% --- Outputs from this function are returned to the command line.
function varargout = shapeFilteringTest_OutputFcn(hObject, eventdata, handles) 
% varargout  cell array for returning output args (see VARARGOUT);
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Get default command line output from handles structure
varargout{1} = handles.output;


% --- Executes on button press in importQcell.
function importQcell_Callback(hObject, eventdata, handles)
% hObject    handle to importQcell (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
try
    qCells = evalin('base','qCells');
catch
    msgbox('Have you imported qCell to workspace?','Error');
    return
end
if ~isstruct(qCells) || isempty(qCells)
    error('Wrong import');
end
handles.qCells = qCells;
% set info in panel
set(handles.importedqCellInfo, 'String', sprintf('Found %d cells',length(handles.qCells)));
% enable showing outline
set(handles.showOutline,'Enable','on');
set(handles.showOutlineMod,'Enable','on');
guidata(hObject,handles);


% --- Executes on button press in loadImage.
function loadImage_Callback(hObject, eventdata, handles)
% hObject    handle to loadImage (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% [FileName,PathName,FilterIndex] = uigetfile('*.*');
PathName = '/home/baniuk/Documents/Quimp-developing/quimp-user-request/';
FileName = 'Composite-after-macro.tif';
handles.imageName = fullfile(PathName,FileName);
handles.stackSize = length(imfinfo(handles.imageName));
updateImage(handles,1);
set(handles.frameSelect,'Min',1,'Max',handles.stackSize);
set(handles.frameSelect,'Value',1,'SliderStep',[1 1]/handles.stackSize);
guidata(hObject,handles);

function updateImage(handles,index)
im = imread(handles.imageName,'index',index);
coords = handles.qCells.outlines{index}(:,2:3);

% calculate new outline
coordsf = handles.process(coords);
xcoordf = coordsf(:,1);
ycoordf = coordsf(:,2);


axes(handles.image);
imagesc(im);
axis equal

% plot outline
if handles.isOutline
    hold on
    plot(coords(:,1),coords(:,2))
    hold off
end
if handles.isOutlineMod
    hold on
    plot(xcoordf,ycoordf,'-r')
    hold off
end
set(handles.image,'xtick',[]);
set(handles.image,'ytick',[]);

% plot outline
axes(handles.outlineImage)
plot(coords(:,1),coords(:,2),'-b')
hold on
plot(xcoordf,ycoordf,'-r');
hold off
grid on
axis square
legend('org','processed');


% --- Executes on slider movement.
function frameSelect_Callback(hObject, eventdata, handles)
% hObject    handle to frameSelect (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'Value') returns position of slider
%        get(hObject,'Min') and get(hObject,'Max') to determine range of slider
nr = round(get(hObject,'Value'));
updateImage(handles,nr);
set(handles.frameNumber,'String',num2str(nr));

% --- Executes during object creation, after setting all properties.
function frameSelect_CreateFcn(hObject, eventdata, handles)
% hObject    handle to slider3 (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: slider controls usually have a light gray background.
if isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor',[.9 .9 .9]);
end


% --- Executes on button press in showOutline.
function showOutline_Callback(hObject, eventdata, handles)
% hObject    handle to showOutline (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of showOutline
if get(hObject,'Value')==0
    handles.isOutline = false;
else
    handles.isOutline = true;
end
guidata(hObject,handles);
updateImage(handles,round(get(handles.frameSelect,'Value')));

% --- Executes during object creation, after setting all properties.
function showOutline_CreateFcn(hObject, eventdata, handles)
% hObject    handle to showOutline (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called
set(hObject,'Enable','off');

% --- Executes on button press in showOutlineMod.
function showOutlineMod_Callback(hObject, eventdata, handles)
% hObject    handle to showOutlineMod (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of showOutlineMod
if get(hObject,'Value')==0
    handles.isOutlineMod = false;
else
    handles.isOutlineMod = true;
end
guidata(hObject,handles);
updateImage(handles,round(get(handles.frameSelect,'Value')));

% --- Executes during object creation, after setting all properties.
function showOutlineMod_CreateFcn(hObject, eventdata, handles)
% hObject    handle to showOutlineMod (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called
set(hObject,'Enable','off');

% --- Executes on button press in fcnMedian.
function fcnMedian_Callback(hObject, eventdata, handles)
% hObject    handle to fcnMedian (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of fcnMedian
if get(hObject,'Value')>0
    params = str2num(get(handles.fcnMedianParam,'String'));
    handles.process = @(in)mediansmooth(in,params);
    set(handles.fcnMedianParam,'Enable','on');
    set(handles.fcnMeanParam,'Enable','off');
    set(handles.fcnMMParam,'Enable','off');
    set(handles.fcnDPParam,'Enable','off');
    set(handles.fcnHampelParam,'Enable','off');
    set(handles.fcnHampelMeanParam,'Enable','off');
end
guidata(hObject,handles);
updateImage(handles,round(get(handles.frameSelect,'Value')));


% --- Executes on button press in fcnMean.
function fcnMean_Callback(hObject, eventdata, handles)
% hObject    handle to fcnMean (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of fcnMean
if get(hObject,'Value')>0
    params = str2num(get(handles.fcnMeanParam,'String'));
    handles.process = @(in)meansmooth(in,params);
    set(handles.fcnMedianParam,'Enable','off');
    set(handles.fcnMeanParam,'Enable','on');
    set(handles.fcnMMParam,'Enable','off');
    set(handles.fcnDPParam,'Enable','off');
    set(handles.fcnHampelParam,'Enable','off');
    set(handles.fcnHampelMeanParam,'Enable','off');
end
guidata(hObject,handles);
updateImage(handles,round(get(handles.frameSelect,'Value')));

% --- Executes on button press in fcnNone.
function fcnNone_Callback(hObject, eventdata, handles)
% hObject    handle to fcnNone (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of fcnNone
if get(hObject,'Value')>0
    params = str2num(get(handles.fcnMeanParam,'String'));
    handles.process = @(in)(in);
    set(handles.fcnMedianParam,'Enable','off');
    set(handles.fcnMeanParam,'Enable','off');
    set(handles.fcnMMParam,'Enable','off');
    set(handles.fcnDPParam,'Enable','off');
    set(handles.fcnHampelParam,'Enable','off');
    set(handles.fcnHampelMeanParam,'Enable','off');
end
guidata(hObject,handles);
updateImage(handles,round(get(handles.frameSelect,'Value')));

% --- Executes on button press in fcnMM.
function fcnMM_Callback(hObject, eventdata, handles)
% hObject    handle to fcnMM (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of fcnMM
if get(hObject,'Value')>0
    params = str2num(get(handles.fcnMMParam,'String'));
    handles.process = @(in)mmsmooth(in,params);
    set(handles.fcnMedianParam,'Enable','off');
    set(handles.fcnMeanParam,'Enable','off');
    set(handles.fcnMMParam,'Enable','on');
    set(handles.fcnDPParam,'Enable','off');
    set(handles.fcnHampelParam,'Enable','off');
    set(handles.fcnHampelMeanParam,'Enable','off');
end
guidata(hObject,handles);
updateImage(handles,round(get(handles.frameSelect,'Value')));

% --- Executes on button press in fcnDP.
function fcnDP_Callback(hObject, eventdata, handles)
% hObject    handle to fcnDP (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of fcnDP
if get(hObject,'Value')>0
    params = str2num(get(handles.fcnDPParam,'String'));
    handles.process = @(in)DPsmooth(in,params);
    set(handles.fcnMedianParam,'Enable','off');
    set(handles.fcnMeanParam,'Enable','off');
    set(handles.fcnMMParam,'Enable','off');
    set(handles.fcnDPParam,'Enable','on');
    set(handles.fcnHampelParam,'Enable','off');
    set(handles.fcnHampelMeanParam,'Enable','off');
end
guidata(hObject,handles);
updateImage(handles,round(get(handles.frameSelect,'Value')));

% --- Executes on button press in fcnHampel.
function fcnHampel_Callback(hObject, eventdata, handles)
% hObject    handle to fcnHampel (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of fcnHampel
if get(hObject,'Value')>0
    params = str2num(get(handles.fcnHampelParam,'String'));
    handles.process = @(in)Hampelsmooth(in,params);
    set(handles.fcnMedianParam,'Enable','off');
    set(handles.fcnMeanParam,'Enable','off');
    set(handles.fcnMMParam,'Enable','off');
    set(handles.fcnDPParam,'Enable','off');
    set(handles.fcnHampelParam,'Enable','on');
    set(handles.fcnHampelMeanParam,'Enable','off');
end
guidata(hObject,handles);
updateImage(handles,round(get(handles.frameSelect,'Value')));

% --- Executes on button press in fcnHampelMean.
function fcnHampelMean_Callback(hObject, eventdata, handles)
% hObject    handle to fcnHampelMean (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hint: get(hObject,'Value') returns toggle state of fcnHampelMean
if get(hObject,'Value')>0
    params = str2num(get(handles.fcnHampelMeanParam,'String'));
    handles.process = @(in)HampelMeansmooth(in,params);
    set(handles.fcnMedianParam,'Enable','off');
    set(handles.fcnMeanParam,'Enable','off');
    set(handles.fcnMMParam,'Enable','off');
    set(handles.fcnDPParam,'Enable','off');
    set(handles.fcnHampelParam,'Enable','off');
    set(handles.fcnHampelMeanParam,'Enable','on');
end
guidata(hObject,handles);
updateImage(handles,round(get(handles.frameSelect,'Value')));


function fcnMMParam_Callback(hObject, eventdata, handles)
% hObject    handle to fcnMMParam (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'String') returns contents of fcnMMParam as text
%        str2double(get(hObject,'String')) returns contents of fcnMMParam as a double
params = str2num(get(hObject,'String'));
handles.process = @(in)mmsmooth(in,params);
guidata(hObject,handles);
updateImage(handles,round(get(handles.frameSelect,'Value')));


function fcnMedianParam_Callback(hObject, eventdata, handles)
% hObject    handle to fcnMedianParam (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'String') returns contents of fcnMedianParam as text
%        str2double(get(hObject,'String')) returns contents of fcnMedianParam as a double
params = str2num(get(hObject,'String'));
handles.process = @(in)mediansmooth(in,params);
guidata(hObject,handles);
updateImage(handles,round(get(handles.frameSelect,'Value')));

function fcnMeanParam_Callback(hObject, eventdata, handles)
% hObject    handle to fcnMeanParam (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'String') returns contents of fcnMeanParam as text
%        str2double(get(hObject,'String')) returns contents of fcnMeanParam as a double
params = str2num(get(hObject,'String'));
handles.process = @(in)meansmooth(in,params);
guidata(hObject,handles);
updateImage(handles,round(get(handles.frameSelect,'Value')));

function fcnDPParam_Callback(hObject, eventdata, handles)
% hObject    handle to fcnDPParam (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'String') returns contents of fcnDPParam as text
%        str2double(get(hObject,'String')) returns contents of fcnDPParam as a double
params = str2num(get(hObject,'String'));
handles.process = @(in)DPsmooth(in,params);
guidata(hObject,handles);
updateImage(handles,round(get(handles.frameSelect,'Value')));

function fcnHampelParam_Callback(hObject, eventdata, handles)
% hObject    handle to fcnHampelParam (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'String') returns contents of fcnHampelParam as text
%        str2double(get(hObject,'String')) returns contents of fcnHampelParam as a double
params = str2num(get(hObject,'String'));
handles.process = @(in)Hampelsmooth(in,params);
guidata(hObject,handles);
updateImage(handles,round(get(handles.frameSelect,'Value')));

function fcnHampelMeanParam_Callback(hObject, eventdata, handles)
% hObject    handle to fcnHampelMeanParam (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Hints: get(hObject,'String') returns contents of fcnHampelMeanParam as text
%        str2double(get(hObject,'String')) returns contents of fcnHampelMeanParam as a double
params = str2num(get(hObject,'String'));
handles.process = @(in)HampelMeansmooth(in,params);
guidata(hObject,handles);
updateImage(handles,round(get(handles.frameSelect,'Value')));


% --- Executes during object creation, after setting all properties.
function fcnMeanParam_CreateFcn(hObject, eventdata, handles)
% hObject    handle to fcnMeanParam (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end

% --- Executes during object creation, after setting all properties.
function fcnMedianParam_CreateFcn(hObject, eventdata, handles)
% hObject    handle to fcnMedianParam (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end


% --- Executes during object creation, after setting all properties.
function fcnMedian_CreateFcn(hObject, eventdata, handles)
% hObject    handle to fcnMedian (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called


% --- Executes during object creation, after setting all properties.
function fcnNone_CreateFcn(hObject, eventdata, handles)
% hObject    handle to fcnNone (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called
set(hObject,'Value',1);
handles.process = @(in)(in);
guidata(hObject,handles);


% --- Executes during object creation, after setting all properties.
function fcnMMParam_CreateFcn(hObject, eventdata, handles)
% hObject    handle to fcnMMParam (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end


% --- Executes during object creation, after setting all properties.
function fcnDPParam_CreateFcn(hObject, eventdata, handles)
% hObject    handle to fcnDPParam (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end

% --- Executes during object creation, after setting all properties.
function fcnHampelParam_CreateFcn(hObject, eventdata, handles)
% hObject    handle to fcnHampelParam (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end

% --- Executes during object creation, after setting all properties.
function fcnHampelMeanParam_CreateFcn(hObject, eventdata, handles)
% hObject    handle to fcnHampelMeanParam (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    empty - handles not created until after all CreateFcns called

% Hint: edit controls usually have a white background on Windows.
%       See ISPC and COMPUTER.
if ispc && isequal(get(hObject,'BackgroundColor'), get(0,'defaultUicontrolBackgroundColor'))
    set(hObject,'BackgroundColor','white');
end
