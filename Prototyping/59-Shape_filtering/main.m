%% About
% Related to http://www.trac-wsbc.linkpc.net:8080/trac/QuimP/ticket/59
% defective frames (Composite-after-macro.tif_Segmentation.tif):
% - 137
% - 75
% - 125
% - 
% Prototype application can be found: shapeFilteringTest.m
%% Load data
addpath('/home/baniuk/Documents/QuimP11_MATLAB/')
qCells = readQanalysis('Resources/after-macro');
testFrames = [75 125 137 1];
clear coords;
for i=1:length(testFrames)
    coords{i} = qCells.outlines{testFrames(i)}(:,2:3);
end
%% Tools - Plot
i = 1;
figure
plot(coords{i}(:,1),coords{i}(:,2))
axis square
grid on
%% Tools - Plot merged
i = 1;
im = imread('Resources/after-macro/Composite-after-macro.tif','index',testFrames(i));
figure;
imagesc(im);axis equal
hold on
plot(coords{i}(:,1),coords{i}(:,2))
%% test 1 (failed)
% Moving window along points. For every W points there is mass center
% calculated and then distances among those points and mass centre.
% Distances are then meaned and centre point dw is moved according to new distance

c = 1;
W = 3;
dw = floor(W/2)+1; % middle window point
newcoords = NaN(length(coords{c}),2);
for i=1:length(coords{c})-W
   cut = coords{c}(i:i+W-1,:); 
   % get mass centre 
   mXY = mean(cut);
   % calculate vector length mXYV, V - verticle
   mXYV = repmat(mXY,[W 1])-cut; % vectors
   LmXYV = sqrt(sum(mXYV,2).^2); % lengths
   % calculate new length
   newLmXYV = mean(LmXYV);
   % get new position of point in middle of window
   % middle vector 
   V = mXYV(dw,:);
   % its versor
   vv = V/(sqrt(sum(V.^2)));
   % and vector of correct length
   vv = vv*newLmXYV;
   % end point coords
   endXY = mXY - vv;
   newcoords(i+dw-1,:) = endXY; 

end

% plotting
figure;
plot(coords{c}(:,1),coords{c}(:,2),'-o')
axis square
grid on
hold on
plot(newcoords(:,1),newcoords(:,2),'r')
%% test 2 (failed)
% Move parallely evey edge
d = 2; % distance
c = 1;
newedge = [];
for i=1:length(coords{c})-1
   edge = coords{c}(i:i+1,:);
   VE = diff(edge); % vector representing edge set in first point
   % get perpendicular vector
   VEp = fliplr(VE).*[1 -1];
   % get versor
   VEPv = VEp/(sqrt(sum(VEp.^2)));
   % new length of versor
   VEPv = VEPv*d;
   % new edge
   newedge = [newedge; edge - repmat(VEPv,2,1)];
end

% detect crossings


% plotting
figure;
plot(coords{c}(:,1),coords{c}(:,2),'-o')
axis square
grid on
hold on
for i=1:2:length(newedge)
    plot(newedge(i:i+1,1),newedge(i:i+1,2),'-rs')
end
%% median smooth
% smooth x and y separatelly by median
% implemented in prototype as mediansmooth
c = 1;
w = 11;
dw = floor(w/2);
xcoord = coords{c}(:,1);
ycoord = coords{c}(:,2);

xcoordf = xcoord;
ycoordf = ycoord;
for i=1:10
    xcoordf = padarray(xcoordf,w,'circular');
    ycoordf = padarray(ycoordf,w,'circular');
    xcoordf = medfilt1(xcoordf,w);
    ycoordf = medfilt1(ycoordf,w);
    xcoordf = xcoordf(w+1:length(xcoord)+w);
    ycoordf = ycoordf(w+1:length(ycoord)+w);
end


figure
plot(xcoord,'b');
hold on
plot(ycoord,'r');
grid on

plot(xcoordf,'--b');
plot(ycoordf,'--r');

% plotting
figure;
plot(xcoord,ycoord,'-b');
hold on
plot(xcoordf,ycoordf,'-r')
axis square
grid on
hold on
%% mean smooth
% implemented in prototype as meansmooth
c = 3;
w = 5;
dw = floor(w/2);
xcoord = coords{c}(:,1);
ycoord = coords{c}(:,2);

xcoordf = xcoord;
ycoordf = ycoord;
for i=1:1
    xcoordf = padarray(xcoordf,w,'circular');
    ycoordf = padarray(ycoordf,w,'circular');
    xcoordf = filter(ones(1,w)/w,1,xcoordf);
    ycoordf = filter(ones(1,w)/w,1,ycoordf);
    xcoordf = xcoordf(w+1:length(xcoord)+w);
    ycoordf = ycoordf(w+1:length(ycoord)+w);
end


figure
plot(xcoord,'b');
hold on
plot(ycoord,'r');
grid on

plot(xcoordf,'--b');
plot(ycoordf,'--r');

% plotting
figure;
plot(xcoord,ycoord,'-b');
hold on
plot(xcoordf,ycoordf,'-r')
axis square
grid on
hold on
%% median + mean
% implemented in prototype as mmsmooth

% combines median and mean one after one
c = 3;
wmedian = 15;
wmean = 3;
biggerw = max([wmedian wmean]);

dw = floor(w/2);
xcoord = coords{c}(:,1);
ycoord = coords{c}(:,2);

xcoordf = xcoord;
ycoordf = ycoord;
for i=1:5
    xcoordf = padarray(xcoordf,biggerw,'circular');
    ycoordf = padarray(ycoordf,biggerw,'circular');
    xcoordf = medfilt1(xcoordf,wmedian);
    ycoordf = medfilt1(ycoordf,wmedian);
    xcoordf = filter(ones(1,wmean)/wmean,1,xcoordf);
    ycoordf = filter(ones(1,wmean)/wmean,1,ycoordf);
    xcoordf = xcoordf(biggerw+1:length(xcoord)+biggerw);
    ycoordf = ycoordf(biggerw+1:length(ycoord)+biggerw);
end


figure
plot(xcoord,'b');
hold on
plot(ycoord,'r');
grid on

plot(xcoordf,'--b');
plot(ycoordf,'--r');

% plotting
figure;
plot(xcoord,ycoord,'-b');
hold on
plot(xcoordf,ycoordf,'-r')
axis square
grid on
hold on
%% test dpsimplify
% Uses Recursive Douglas-Peucker Polyline Simplification that originally
% detects most important points on curve.
% ix - indexes retained
addpath('dpsimplify/')
c = 3;
coord = coords{c}(:,1:2);
iter = 5;
psin = coord;
figure
for i=1:iter
    lin = 1:length(psin);
    [~,ix] = dpsimplify(psin,1/exp(-(i)*0.1));
    ixx = setdiff(lin,ix); % get those rejected points which are more interesting for us
    psout = psin(ixx',:);
    psin = psout;
    psin = [coord(1,:); psin; coord(end,:)]; 
    
    % plotting
    subplot(2,3,i)
    plot(coord(:,1),coord(:,2),'-b','markersize',0.5);
    hold on
    plot(psin(:,1),psin(:,2),'-ro','markersize',3)
    %plot(psx(:,1),psx(:,2),'-go')
    axis square
    grid on
    hold off
    title(i)
end
ps = psout;


%% smoothing between verticles (do not work)
addpath('dpsimplify/')
c = 3;
coord = coords{c}(:,1:2);
iter = 5;
% [out,ix,ixx] = DPsmooth(coord,iter);

lin = 1:length(coord);
[~,ix] = dpsimplify(coord,0.05)
ixx = setdiff(lin,ix);
out = coord(ixx',:);
out = [coord(1,:); out; coord(end,:)];
ixx = [1, ixx, lin(end)];

I = [];
for i=1:length(ixx)-1
    x = ixx(i):ixx(i+1);
    y = coord(x,:);
%     xinterp = interp1(x, y(:,1),x,'spline');
%     yinterp = interp1(x, y(:,2),x,'spline');

    I = [I; [xinterp' yinterp']];
end


figure
plot(coord(:,1),coord(:,2),'-bs','markersize',5);
hold on
plot(out(:,1),out(:,2),'-ro','markersize',3)
plot(I(:,1),I(:,2),'-go','markersize',3)

axis square
grid on
hold off

%% hampel
c = 4;

coord = coords{c};
[coordf,j] = hampel(coord,25,0.8);
hh = ~any(j')';
coordf = coordf(hh,:);
if hh(1)==0
    coordf = [coord(1,:); coordf];
end
if hh(end)==0
    coordf = [coordf; coord(end,:)];
end
% add extra smoothing after hampel (with small window)

coordff = meansmooth(coordf,[3,1]);
coordff = [coord(1,:); coordff; coord(end,:)];

figure
plot(coord(:,1),coord(:,2),'-bs','markersize',5);
hold on
plot(coordf(:,1),coordf(:,2),'-ro','markersize',3)
plot(coordff(:,1),coordff(:,2),'-go','markersize',3)

axis square
grid on
hold off

