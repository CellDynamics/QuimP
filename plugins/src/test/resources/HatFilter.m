%% info
% see main.m in Prototyping as well for real case test generator

%% hatfilter test generator
in = [1:40; zeros(1,40)];
in(:,19:21) = [19:21;1 1 1];
in = in';
plot(in(:,1),in(:,2),'-o'); grid on

% based on hatsmooth
crown = 5;
brim = 3;
sig = 0.05;

coord = in;
clear ratio
dp = floor(crown/2);
coordp = padarray(coord,dp,'circular');
start = dp+1;
indtoremove = cell(1,length(start:length(coord)+start-1));
for i=start:length(coord)+start-1
    allpoints = coordp(i-dp:i+dp,:);
    allvectors = diff(allpoints);
    lenallvectors = sum(sqrt(sum(allvectors.^2,2)))';
    nocrownpoints = allpoints;
    nocrownpoints(dp+1-floor(brim/2):dp+1+floor(brim/2),:) = [];
    nocrownvectors = diff(nocrownpoints);
    lennocrownvectors = sum(sqrt(sum(nocrownvectors.^2,2)))';
    ratio(i-start+1) = 1-lennocrownvectors/lenallvectors;    
end

indtoremovetest = [];
for i=1:length(ratio)
    if(ratio(i))>sig
        indtoremove{i} = (i+start-1) - floor(crown/2):(i+start-1)+floor(crown/2);
        indtoremovetest = [indtoremovetest indtoremove{i}];
    end
end

% set NaN for vertexes to remove
for i=1:length(indtoremove)
   if ~isempty(indtoremove{i})
       coordp(indtoremove{i},:) = NaN;
   end
end
% delete padding (on beginig)
coordp(1:dp,:) = [];
% and on the end
coordp = coordp(1:length(coord),:);
% find positions of NaNs (vertices to remove)
isnotnan = ~any(isnan(coordp),2);
% and remove them
coordpp = coordp(isnotnan,:);

hold on
plot(coordpp(:,1),coordpp(:,2),'-rs')
hold off
figure
plot(ratio);grid on
hold on
plot(coordpp(:,1),repmat(sig,length(coordpp),1),'-g')
hold off
unique(indtoremovetest)

%% circular object
clear x y
a = 0:6:359;
x = 20*cosd(a);
y = 20*sind(a);
figure;plot(x,y,'o')
axis square
xy = [x' y'];
xyr = reshape(xy',[],1); % x first
fid = fopen('testData_circle.dat', 'w');
fprintf(fid,'%.4f\n',xyr);
fclose(fid);
%% protrusions - generate test data
% generate test case with protrusions

Ro = 150;
R1 = Ro/5;
R2 = Ro/8;
R3 = Ro/2;
R4 = Ro/12;
% get size of lightField
ls = linspace(-250,250,500);
[X, Y] = meshgrid(ls,ls);
% create empty light field
Object = zeros(size(X,1),size(Y,2));
% filling the circle directly from equation
Object(X.^2+Y.^2<=Ro^2) = 255;
Object((X-max(ls)/2.2).^2+(Y-max(ls)/2.2).^2<=R1^2) = 255;
Object((X+max(ls)/2.4).^2+(Y+max(ls)/2.4).^2<=R2^2) = 255;
Object((X+max(ls)/3.8).^2+(Y-max(ls)/3.8).^2<=R3^2) = 255;
Object((X-60).^2+(Y+130).^2<=R2^2) = 0;
Object((X-max(ls)*0.6).^2/4+(Y-max(ls)*0.0).^2<=R4^2) = 255;

% control plotting
figure;imagesc(ls,ls,Object); axis square;colormap gray
B = bwboundaries(Object);
xy = B{1}(1:6:end,:);
xyr = reshape(xy',[],1); % x first
fid = fopen('testData_prot.dat', 'w');
fprintf(fid,'%.4f\n',xyr);
fclose(fid);
save prot.mat xy
%% protrusions - load java results and comapre with matlab

clc;out = hatsmooth_deb(xy,[9 3 1 0]); % compare logs with this version
figure;plot(xy(:,1),xy(:,2),'go');grid on;axis square
hold on
plot(out(:,1),out(:,2),'rs')

% load output from test
im = imread('/tmp/test_HatFilter_run_2.tif');
figure;imagesc((im)); axis square
hold on
% plot(out(:,1)-(135-66),out(:,2)-(132-62),'rs','MarkerFaceColor','r') % manual matching
plot(xy(:,1)-(135-66),xy(:,2)-(132-62),'go')
legend('orginal','removed')
title('Test image on background - compared to matlab results')