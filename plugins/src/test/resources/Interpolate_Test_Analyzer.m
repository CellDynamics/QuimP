% Load and plot results of Interpolate_Test
p = '/tmp/';
d = dir(fullfile(p,'test_getInterpolationLoess_*'));
o = dir(fullfile(p,'test_roiSaver_*'));

for i=1:length(o)
    di = imread(fullfile(p,d(i).name));
    oi = imread(fullfile(p,o(i).name));
    figure;
    subplot(1,2,1);
    imagesc(di);colormap gray;axis square;title(d(i).name, 'Interpreter', 'none','fontsize',8);
    subplot(1,2,2);
    imagesc(oi);colormap gray;axis square;title(o(i).name, 'Interpreter', 'none','fontsize',8);
    
end
%%
p = '/tmp/';
d = dir(fullfile(p,'test_getInterpolationMean_*'));
o = dir(fullfile(p,'test_roiSaver_*'));

for i=1:length(o)
    di = imread(fullfile(p,d(i).name));
    oi = imread(fullfile(p,o(i).name));
    figure;
    subplot(1,2,1);
    imagesc(di);colormap gray;axis square;title(d(i).name, 'Interpreter', 'none','fontsize',8);
    subplot(1,2,2);
    imagesc(oi);colormap gray;axis square;title(o(i).name, 'Interpreter', 'none','fontsize',8);
    
end