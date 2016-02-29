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
%% Generate test cases
% should be copied to relevant test/Resources directory
for i=1:length(testFrames)
   fid = fopen(['testData_' num2str(testFrames(i)) '.dat'], 'w');
   xy = coords{i};
   xyr = reshape(xy',[],1); % x first
   fprintf(fid,'%.4f\n',xyr);
   fclose(fid);
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

coordff = meansmooth(coordf,[5,1]);
% last and first point are often lost but sometimes they can not be just
% added as inserted originals. See example for first frame (c=4). It does
% not work because in this case both points are on protrusion.

% coordff = [coord(1,:); coordff; coord(end,:)];

figure
plot(coord(:,1),coord(:,2),'-bs','markersize',5);
hold on
plot(coordf(:,1),coordf(:,2),'-ro','markersize',3)
plot(coordff(:,1),coordff(:,2),'-go','markersize',3)

axis square
grid on
hold off
%% vector angles (test)
c = 3;
coord = coords{c};
clear angle
for i = 1:length(coord)-2
    v1c = coord(i:i+1,:);
    v2c = coord(i+1:i+2,:);
    v1 = diff(v1c);
    v2 = diff(v2c);
    v1 = v1/sqrt(sum(v1.^2));
    v2 = v2/sqrt(sum(v2.^2));
    angle(i+1) = acosd(v1*v2');
end

figure
plot(coord(:,1),coord(:,2),'-bs','markersize',5);
figure
plot(1:length(angle),angle,'-b');

w = 23;
dw = floor(w/2);
anglep = padarray(angle',w,'symmetric')';
anglep = diff(anglep);
cs = [];
for i = dw+1:length(angle)+dw
    cs(i-dw) = sum(anglep(i-dw:i+dw));
    
end
hold on
plot(1:length(angle),cs,'-r');

xcs = find(abs(cs)<10);
coordm = coord;
coordm(xcs',:)=[];
figure
plot(coord(:,1),coord(:,2),'-bs','markersize',5);
hold on
plot(coordm(:,1),coordm(:,2),'-rs','markersize',5);
%% hat filtering
% Hat filter slides over envelope. For every position there are two lengths
% calculated: Length of curve covered by hat (crown + brim) nad length of
% curve without crown. For small survatures those lengths should be
% similar. If they are not (ratio) vertexes from crown are removed.
c = 3;
coord = coords{c};
clear angle ratio indtoremove ar indtoremovealt
crown = 13;
brim = 5;
dp = floor(crown/2)+brim;
coordp = padarray(coord,crown+2*brim,'circular');
start = brim+floor(crown/2)+1;
indtoremove = cell(1,length(start:length(coord)+start-1));
for i=start:length(coord)+start-1
    allpoints = coordp(i-dp:i+dp,:);
    allvectors = diff(allpoints);
    lenallvectors = sum(sqrt(sum(allvectors.^2,2)));
    nocrownpoints = allpoints;
    nocrownpoints(brim+1:end-brim,:) = [];
    nocrownvectors = diff(nocrownpoints);
    lennocrownvectors = sum(sqrt(sum(nocrownvectors.^2,2)));
    ratio(i-start+1) = 1-lennocrownvectors/lenallvectors;
    
    % test for other criterion
    crownpoints = allpoints(brim+1:end-brim,:);
    ar(i-start+1) = polyarea(crownpoints(:,1),crownpoints(:,2))/polyarea(allpoints(:,1),allpoints(:,2));
    
end

for i=1:length(ratio)
    if(ratio(i))>0.28
        indtoremove{i} = (i+start-1) - floor(crown/2):(i+start-1)+floor(crown/2);
%         indtoremove{i} = (i+start-1); % crown=29;brim=7;
    end
end

% % alternative criterion
% [xData, yData] = prepareCurveData( [], ratio );
% 
% ft = fittype( 'poly1' );
% [fitresult, gof] = fit( xData, yData,ft );
% 
% ratiofit = fitresult(xData);
% for i=1:length(xData)
%     if ratiofit(i)<ratio(i)
% %         indtoremove{i} = (i+start-1) - floor(crown/2):(i+start-1)+floor(crown/2);
%         indtoremove{i} = (i+start-1);
%     end
% end
% figure;plot(ratio,'-bo');hold on; plot(ratiofit,'-rs');

% set NaN for vertexes to remove
for i=1:length(indtoremove)
   if ~isempty(indtoremove{i})
       coordp(indtoremove{i},:) = NaN;
   end
end
% delete padding (on beginig)
coordp = coordp(crown+2*brim+1:end,:);
% and on the end
coordp = coordp(1:length(coord),:);
% find positions of NaNs (vertices to remove)
isnotnan = ~any(isnan(coordp),2);
% and remove them
coordpp=coordp(isnotnan,:);

% smooth
[xDatax, yDatax] = prepareCurveData( [], coordpp(:,1) );
[xDatay, yDatay] = prepareCurveData( [], coordpp(:,2) );
ft = fittype( 'smoothingspline' );
opts = fitoptions( 'Method', 'SmoothingSpline' );
opts.SmoothingParam = 0.1;
[fitresult, gof] = fit( xDatax, yDatax, ft, opts );
coordppf(:,1) = fitresult(xDatax);
[fitresult, gof] = fit( xDatay, yDatay, ft, opts );
coordppf(:,2) = fitresult(xDatay);

figure
plot(coord(:,1),coord(:,2),'-bs','markersize',5);
hold on
plot(coordpp(:,1),coordpp(:,2),'-rs','markersize',5);
plot(coordppf(:,1),coordppf(:,2),'-k');
%% roundness
% The sliding window of given size is moved along
% outline. For every position of the window its content is
% removed and for such new outline the circularity is
% computed. Its is expected that curved parts of outline 
% affect shape the most therefore, circularity without
% these parts will differ from original one. Such differences
% are calculated for every position of the window and then points
% for window for which the biggest difference was denoted are
% removed. This is done in a loop where a user can set number
% of windows to remove. Windows can not overlap so checking
% of edges of sections to remove is performed.
% Additionally to eliminate cases where points laying on line are removed,
% for all candidates there is additional factor evaluated Pc (different
% methods are proposed. This factor weights circularity.
% All constructions are:
% 0. Biggest circularity coefficient must be greater than acceptance level
% 1. windows can not overlap
% 2. All window pixels must be convex comparing to polygon without these
% pixels
% Error is thrown if there are no available candidates according to these
% rules.

% Point 0 allows switchnig off algorithm for certain images.


c = 3;
w = 15;

wp = floor(w/2);

coord = coords{c};

ind = padarray([1:length(coord)]',wp,'circular');

X = coord(:,1); Y = coord(:,2);
Xt = [X; X(1)];
Yt = [Y; Y(1)];
dx = diff(Xt); dy = diff(Yt);
P = sum(sqrt(dx.^2+dy.^2));
A = polyarea(X,Y);
circ = (4*pi*A)/(P.^2);

l = 1;
cc = [];
Pc = [];
for i=wp+1:length(coord)
    indtorem = ind(i-wp:i+wp);
    coordrem = coord;
    coordrem(indtorem,:) = [];
    A = polyarea(coordrem(:,1),coordrem(:,2));
    coordrem = [coordrem;coordrem(1,:)];
    d = diff(coordrem);
    P = sum(sqrt(sum(d.^2,2)));
    
    coordrem = coord(indtorem,:);
    
      % candidate 1
%     center = coordrem(wp+1,:);
%     d = coordrem - repmat(center,length(coordrem),1);
%     Pc = [Pc std(sqrt(sum(d.^2,2)))];
    
    % candidate 2
    center = coordrem(wp+1,:); %coordrem(wp+1,:);
    d = coordrem - repmat(center,length(coordrem),1);
    Pc = [Pc mean(sqrt(sum(d.^2,2)))];
    
    
    
    cc = [cc (4*pi*A)/(P.^2)];
end
cc = cc./(Pc);
cc = cc/circ;

ccsort = sort(cc,'descend');

ile = 3;
level = 0; 
if ccsort(1)>level
    coordrem = coord;
    clear indtorem;
    i = 1;
    found = 0;
    while(found<ile)
        if(i>length(ccsort))
            warning('Can find next candidate. Use smaller window');
            break;
        end
        m = find(cc==ccsort(i));    
        m = m + wp+1;
        if found>0
            sub = indtorem;   % all previous cases (indexes)
            mmsub = minmax(sub(:)');    % range of previous results
            mmcurr = minmax(ind(m-wp:m+wp)'); % current indexes (candidates)
            % check if current indexes are common with any of previous cases
            if mmcurr(2) < mmsub(1) % maximum current < min prev
                % found candidate
                found = found + 1;
                disp([find(cc==ccsort(i)) ccsort(i)])
                i = i + 1;
                indtorem(found,:) = ind(m-wp:m+wp);
            else
                if mmcurr(1) > mmsub(2) % min current > max prev
                    % found candidate
                    found = found + 1;
                    disp([find(cc==ccsort(i)) ccsort(i)])
                    i = i + 1;
                    indtorem(found,:) = ind(m-wp:m+wp);
                else
                    i = i + 1; % check next
                    continue;
                end
            end
        else
            disp([find(cc==ccsort(i)) ccsort(i)])
            i = i + 1; % for one accept it and go to next candidate
            found = found + 1;
            indtorem(found,:) = ind(m-wp:m+wp);
        end
        % verify if found protrusion is inside or ouside polygon
        % temporary remove just found indexes
        it = indtorem(found,:);
        tmppol = coord;
        tmppol(it,:) = [];
        tp = coord(it,:); % middle point of window
        if(any( inpolygon(tp(:,1),tp(:,2),tmppol(:,1),tmppol(:,2)) ))
            disp('ins');
            % delete found
            indtorem(found,:) = [];
            found = found - 1;
        end

    end
    coordrem(reshape(indtorem,1,[]),:) = [];
else
    coordrem = coord;
end

% final processing results can be filtered by running mean or running
% median what helps in dealing with particular points on end or beginig of
% removed window.
figure
plot(coord(:,1),coord(:,2),'-bs','markersize',5);
hold on
plot(coordrem(:,1),coordrem(:,2),'-rs','markersize',5);
%% test of hatsmooth for previous algorithm
c = 3;
w = 15;
ile = 2;

coord = coords{c};

out = hatsmooth(coord,[w,ile,1]);

figure
plot(coord(:,1),coord(:,2),'-bs','markersize',5);
hold on
plot(out(:,1),out(:,2),'-rs','markersize',5);