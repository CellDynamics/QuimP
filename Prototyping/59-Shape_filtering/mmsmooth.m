function out = mmsmooth(in,params)
% apply running median and runnig mean on vector in
% params: [window_median window_mean iterations]

wmedian = params(1);
wmean = params(2);
biggerw = max([wmedian wmean]);
iter = params(3);

xcoordf = in(:,1);
ycoordf = in(:,2);

for i=1:iter
    xcoordf = padarray(xcoordf,biggerw,'circular');
    ycoordf = padarray(ycoordf,biggerw,'circular');
    xcoordf = medfilt1(xcoordf,wmedian);
    ycoordf = medfilt1(ycoordf,wmedian);
    xcoordf = filter(ones(1,wmean)/wmean,1,xcoordf);
    ycoordf = filter(ones(1,wmean)/wmean,1,ycoordf);
    xcoordf = xcoordf(biggerw+1:length(in(:,1))+biggerw);
    ycoordf = ycoordf(biggerw+1:length(in(:,2))+biggerw);
end

out(:,1) = xcoordf;
out(:,2) = ycoordf;