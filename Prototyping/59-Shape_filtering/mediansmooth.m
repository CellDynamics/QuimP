function out = mediansmooth(in,params)
% apply running median on vector in
% params: [window iterations]

w = params(1);
iter = params(2);

xcoordf = in(:,1);
ycoordf = in(:,2);

for i=1:iter
    xcoordf = padarray(xcoordf,w,'circular');
    ycoordf = padarray(ycoordf,w,'circular');
    xcoordf = medfilt1(xcoordf,w);
    ycoordf = medfilt1(ycoordf,w);
    xcoordf = xcoordf(w+1:length(in(:,1))+w);
    ycoordf = ycoordf(w+1:length(in(:,2))+w);
end

out(:,1) = xcoordf;
out(:,2) = ycoordf;