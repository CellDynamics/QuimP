%% info
% see main.m in Prototyping as well for real case test generator

%% circular object - test case
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
% results from matlab may differ because java has newer version of
% algorithm

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
%% view results of parametrized tests hatfilter
p = '/tmp/';
d = dir(fullfile(p,'test_HatFilter_testData*'));
o = dir(fullfile(p,'ref_HatFilter_testData*'));

for i=1:length(o)
    di = imread(fullfile(p,d(i).name));
    oi = imread(fullfile(p,o(i).name));
    figure;
    subplot(1,2,1);
    imagesc(di);colormap gray;axis square;title(d(i).name, 'Interpreter', 'none','fontsize',8);
    subplot(1,2,2);
    imagesc(oi);colormap gray;axis square;title(o(i).name, 'Interpreter', 'none','fontsize',8);
    
end