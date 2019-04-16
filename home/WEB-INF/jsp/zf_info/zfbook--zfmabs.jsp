<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<zfin2:page title="ZFIN The Zebrafish Server">
<div>
For the most current and complete antibody data, use the <a href="/action/antibody/search"> Antibody Search Page</a>
<br>
To see ZIRC antibodies only, set the search to "Show only ZIRC antibodies".
<p></p>
<b>Monoclonal antibodies</b> that recognize a variety of structures in zebrafish embryos are available
from the library at the <a href="http://zebrafish.org/">Zebrafish International Resource Center</a>.
Please feel free to communicate with us via email if you have any questions:
<a href="mailto:fish_requests@zebrafish.org">fish_requests@zebrafish.org</a>
<p>
<input name="requestAntibodies" onclick="window.location.href='/zirc/accounts/mtaAgree.php?source=guide&dest=antibodies'" type="button" value="Request Antibodies">
</p><hr>
<h2>The following is a partial list of available antibodies at ZIRC:</h2>
<i>(Source: K. Larison and B. Trevarrow)</i>
<p>
    (Abbreviations are listed at the end.)
    <br>
<br>
</p><table border="0" cellspacing="10">
<tr>
<td class="big"><b><u>Name</u></b></td>
<td class="big"><b><u>Type</u></b></td>
<td class="big"><b><u>Staining</u></b></td>
<td class="big"><b><u>Structures stained</u></b></td>
</tr>
<tr>
<td class="medium">anti-Dlx3b</td>
<td class="medium">IgG1,k</td>
<td class="medium">nuclear</td>
<td class="medium">neural plate border, otic preplacode, otic placode, olfactory, finfold</td>
</tr>
<tr>
<td class="medium">anti-Vegt<br>(aka "anti-Spadetail")</td>
<td class="medium">IgG2a,k</td>
<td class="medium">nuclear</td>
<td class="medium">tailbud & adaxial cells; presomitic, lateral mesoderm, and prechordal plate cells</td>
</tr>
<tr>
<td class="medium">zl-1</td>
<td class="medium">IgG1</td>
<td class="medium">cyto or ecm</td>
<td class="medium">lns</td>
</tr>
<tr>
<td class="medium">zm-1</td>
<td class="medium">&#160;</td>
<td class="medium">gran cyto</td>
<td class="medium">mus,act?,sheet mus over yolk(96h),fnv,hrt,musoc,muse,musb,fnpec,fnv</td>
</tr>
<tr>
<td class="medium">zm-2</td>
<td class="medium">&#160;</td>
<td class="medium">gran cyto</td>
<td class="medium">mus/sub skin of fnd,act,sheet over cns(96h)</td>
</tr>
<tr>
<td class="medium">zm-3</td>
<td class="medium">&#160;</td>
<td class="medium">gran cyto</td>
<td class="medium">som,mus,myo,str,musb,muse,musoc</td>
</tr>
<tr>
<td class="medium">zn-1</td>
<td class="medium">IgG1</td>
<td class="medium">cyto</td>
<td class="medium">blm,ysl,ne u,axn,har,drg,rbn,pmn,ens,pns</td>
</tr>
<tr>
<td class="medium">zn-2</td>
<td class="medium">&#160;</td>
<td class="medium">surf</td>
<td class="medium">npl, lns,hin clusters,spi roots,har,for cells, tri, trg, npl, rbn, visc organ</td>
</tr>
<tr>
<td class="medium">zn-3</td>
<td class="medium">&#160;</td>
<td class="medium">surf</td>
<td class="medium">proc,npl, neu,cns surf,pns,hin segs,fnp,ens,pns,npl,bns,ipl,opl,some nerve tracts</td>
</tr>
<tr>
<td class="medium">zn-4</td>
<td class="medium">&#160;</td>
<td class="medium">cyto or surf</td>
<td class="medium">neu,har,npl,proc,pin,hin npl seg,spi seg?,fin cells & proc, orc,lining of visc organ, outside of
        ins, ncr(?), ipl, gan, eye neu
    </td>
</tr>
<tr>
<td class="medium">zn-5</td>
<td class="medium">IgG1</td>
<td class="medium">surf</td>
<td class="medium">rbn, hin clusters, vsn, mps, per gang cell, gan, some cns pathways, ncr(?), npl</td>
</tr>
<!--- 18-Nov-2005 rholland - No longer availabe through ZIRC
  <tr>
    <td class="medium">zn-6</td>
    <td class="medium">IgG1</td>
    <td class="medium">surf</td>
    <td class="medium">long spi tracts or npl,dsn,mussf, npl, ens nrt, pns, ipl,opl,gan</td>
  </tr>
  <tr>
    <td class="medium">zn-7</td>
    <td class="medium">IgG1</td>
    <td class="medium">cyto</td>
    <td class="medium">spi,brn,pin,har,around gut, snl, nrt, pln, plg, ncr, nVIIIg</td>
  </tr>
--->
<tr>
<td class="medium">zn-8</td>
<td class="medium">IgG1</td>
<td class="medium">surf</td>
<td class="medium">periodic cells between som surf & skin, hsp, some cells in brn, rbn, vsn, som, hin clusters</td>
</tr>
<tr>
<td class="medium">zn-9</td>
<td class="medium">IgG1</td>
<td class="medium">surf(?)</td>
<td class="medium">hin verticle proc seg,tri,trg,sln,hin npl,ens, pns, opl, fin(lt)</td>
</tr>
<!--- 18-Nov-2005 rholland - No longer availabe through ZIRC
  <tr>
    <td class="medium">zn-10</td>
    <td class="medium">IgG1</td>
    <td class="medium">(?)</td>
    <td class="medium">ysl,neu,har,pns,nrt,ipl,op,drg,ens,npl</td>
  </tr>
  <tr>
    <td class="medium">zn-11</td>
    <td class="medium">IgG1</td>
    <td class="medium">(?)</td>
    <td class="medium">evl,har,pin,neu,fibers,hin seg,pns gang, gan, npl, drg</td>
  </tr>
--->
<tr>
<td class="medium">zn-12</td>
<td class="medium">IgG1</td>
<td class="medium">surf</td>
<td class="medium">rbn,dsn,proc in cns & pns, mus, myc, nrt, hin clusters, fin, bns, ens, ntd(1 month)</td>
</tr>
<tr>
<td class="medium">zn-13</td>
<td class="medium">IgM</td>
<td class="medium">surf</td>
<td class="medium">rbn,dsn,trg,tri,mus,myc,har,npl(lt),ens</td>
</tr>
<!--- 18-Nov-2005 rholland - No longer availabe through ZIRC
  <tr>
    <td class="medium">zn-14</td>
    <td class="medium">IgM</td>
    <td class="medium">surf</td>
    <td class="medium">neu(mediumer),har,ncr(?)npl,pns,ipl,opl,seg hin structures, cells on outside of spi</td>
  </tr>
--->
<tr>
<td class="medium">zn-15</td>
<td class="medium">IgG1</td>
<td class="medium">cyto or surf(?)</td>
<td class="medium">proc, npl, neu,hin seg, har, pin, cns, pns, ens, pln, opl, ipl, blood vessels in brn(?)</td>
</tr>
<tr>
<td class="medium">zna-1</td>
<td class="medium">IgG1</td>
<td class="medium">&#160;</td>
<td class="medium">fibers,npl,hin seg,growth cones, mus spag, some somata</td>
</tr>
<tr>
<td class="medium">znp-1</td>
<td class="medium">IgG2a,k</td>
<td class="medium">surf</td>
<td class="medium">npl,vent roots,hin seg,ipl,mus spag,ysl, some spi cells</td>
</tr>
<tr>
<td class="medium">znp-2</td>
<td class="medium">IgG1</td>
<td class="medium">&#160;</td>
<td class="medium">ipl,opl,base of har,spi npl,bnp</td>
</tr>
<tr>
<td class="medium">znp-3</td>
<td class="medium">&#160;</td>
<td class="medium">&#160;</td>
<td class="medium">npl,bnp,snv</td>
<td class="medium"></td>
<!--- 18-Nov-2005 rholland - No longer availabe through ZIRC
      <tr>
        <td class="medium">znp-4</td>
        <td class="medium">IgG1</td>
        <td class="medium">surf</td>
        <td class="medium"> npl,opl,ipl,spi npl,gan axons</td>
      </tr>
    --->
</tr><tr>
<td class="medium">znp-6</td>
<td class="medium">&#160;</td>
<td class="medium">&#160;</td>
<td class="medium">evl,ysl,cns,nrt,lns,som,npl,sln,svn,myc,visc organs, gills (nerves?)</td>
</tr>
<tr>
<td class="medium">znp-7</td>
<td class="medium">&#160;</td>
<td class="medium">surf</td>
<td class="medium">npl,bnp,snl,snv,ipl,opl(double layer),hin seg, ysl, har(base), some neu in nrt & spi</td>
</tr>
<!--- 22-Mar-2006 rholland - Not available through ZIRC
  <tr>
    <td class="medium">znp-8</td>
    <td class="medium">IgG1</td>
    <td class="medium">surf</td>
    <td class="medium">dee,neu(?),spi roots,hin seg npl & cells,seg lter shafts in npl, outline central canal</td>
  </tr>
--->
<tr>
<td class="medium">zns-1</td>
<td class="medium">IgG1</td>
<td class="medium">cyto(?) surf</td>
<td class="medium">cns,pns,cells & proc,hin seg,ens,opl,ipl,growing edge of retina stains different from rest</td>
</tr>
<tr>
<td class="medium">zns-2</td>
<td class="medium">&#160;</td>
<td class="medium">surf</td>
<td class="medium">npl,bnp,raf in hin,har,olf,opl,ipl,ens,act or fin proc</td>
</tr>
<!--- 24-Aug-2006 rholland - zns-3 now available through ZIRC --->
<tr>
<td class="medium">zns-3</td>
<td class="medium">IgG1</td>
<td class="medium">cyto,surf(?)</td>
<td class="medium">ysl,mus,per,neu,har,pns,str</td>
</tr>
<!--- 22-Mar-2006 rholland - Not available through ZIRC
  <tr>
    <td class="medium">zns-4</td>
    <td class="medium">&nbsp;</td>
    <td class="medium">cyto,surf(?)</td>
    <td class="medium">cns,har,pns,raf(?),npl,bns,ens,proc</td>
  </tr>
--->
<tr>
<td class="medium">zns-5</td>
<td class="medium">&#160;</td>
<td class="medium">surf</td>
<td class="medium">cns,oto,har,pns,ens,bns,fin fibers,opl,ipl,hin seg</td>
</tr>
<tr>
<td class="medium">zns-6</td>
<td class="medium">&#160;</td>
<td class="medium">&#160;</td>
<td class="medium">cns,fibers,raf,npl,opl,ipl,myc,ect,hin seg</td>
</tr>
<tr>
<td class="medium">zns-7</td>
<td class="medium">&#160;</td>
<td class="medium">cyto</td>
<td class="medium">evl,ysl,dee,lns,cns,ntd,som,gut,npl ,cells in oral epithelium,hsp,myc</td>
</tr>
<tr>
<td class="medium">znt-1</td>
<td class="medium">IgM</td>
<td class="medium">cyto or ecm</td>
<td class="medium">ntd,around lns,act fnd & fnv,myc,btw ect & som,fnp tip,npl,har,part of oto,ret edge,ipl</td>
</tr>
<tr>
<td class="medium">znu-1</td>
<td class="medium">&#160;</td>
<td class="medium">&#160;</td>
<td class="medium">nuclei,chromosomes,some cyto, all tissues except possibly ysl</td>
</tr>
<tr>
<td class="medium">znu-2</td>
<td class="medium">&#160;</td>
<td class="medium">&#160;</td>
<td class="medium">nuclei,dee,evl,outer layer of lns,rbn,nrt, scattered cells in brn</td>
</tr>
<tr>
<td class="medium">zpr-1</td>
<td class="medium">&#160;</td>
<td class="medium">cyto(?)</td>
<td class="medium">phr, pin, long double cones</td>
</tr>
<tr>
<td class="medium">zpr-2</td>
<td class="medium">&#160;</td>
<td class="medium">cyto</td>
<td class="medium">pin,phr</td>
</tr>
<tr>
<td class="medium">zpr-3</td>
<td class="medium">&#160;</td>
<td class="medium">cyto(?)</td>
<td class="medium">phr,rod outer segments,many other tissues at 1 month</td>
</tr>
<tr>
<td class="medium">zrf-1</td>
<td class="medium">IgG1</td>
<td class="medium">cyto(?)</td>
<td class="medium">rafv spi & hin seg, vent roots, dor-vent tract in brn, ntd(lt), lining around gut, raf in nrt
    </td>
</tr>
<tr>
<td class="medium">zrf-2?</td>
<td class="medium">&#160;</td>
<td class="medium">cyto(?)</td>
<td class="medium">raf thru cns dker in spi & hin</td>
</tr>
<tr>
<td class="medium">zrf-3</td>
<td class="medium">IgG1</td>
<td class="medium">surf</td>
<td class="medium">raf,brn edge,hin seg raf,pln,ipl,opl,sln, pattern on top of brn</td>
</tr>
<tr>
<td class="medium">zrf-4</td>
<td class="medium">&#160;</td>
<td class="medium">surf</td>
<td class="medium">raf,hin seg raf,some cell outlines,cns,optic nerve</td>
</tr>
<!--- 18-Nov-2005 rholland - No longer availabe through ZIRC
  <tr>
    <td class="medium">zs-1</td>
    <td class="medium">&nbsp;</td>
    <td class="medium">cyto</td>
    <td class="medium">nrt,ipl,outer gut lining,pharynx,ysl</td>
  </tr>
--->
<tr>
<td class="medium">zs-2</td>
<td class="medium">&#160;</td>
<td class="medium">cyto(?)</td>
<td class="medium">outer lns,nrt,fiber tract in brn,inner segs of rod(kdl), nrt npl(lt)</td>
</tr>
<tr>
<td class="medium">zs-3</td>
<td class="medium">&#160;</td>
<td class="medium">cyto</td>
<td class="medium">nrt</td>
</tr>
<tr>
<td class="medium">zs-4</td>
<td class="medium">&#160;</td>
<td class="medium">cyto</td>
<td class="medium">evl,ysl,dee,cns,lns,mus,fiber tract in brn,outer layer of lns cells,gan & other nrt
        cells,iplk,opl, adult nrt only rod inner segm
        ents(cryo-kdl)
    </td>
</tr>
</table>
<table border="0" cellspacing="0">
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>ABBREVIATIONS</td>
<td>&#160;</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>&#160;</td>
<td>&#160;</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>act</td>
<td>actinotrichia</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>alg</td>
<td>anterior lateral line ganglion</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>aln</td>
<td>anterior lateral line nerve</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>alp</td>
<td>anterior lateral line primordium</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>ama</td>
<td>retinal amacrine neuron</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>axn</td>
<td>axon</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>bip</td>
<td>retinal bipolar neuron</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>bld</td>
<td>blood</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>blm</td>
<td>blastomere</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>bnp</td>
<td>brain neuropil</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>bns</td>
<td>branchial nervous system</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>bra</td>
<td>branchial arch</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>brn</td>
<td>brain</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>brp</td>
<td>branchial pouch</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>cad</td>
<td>caudal hindbrain level</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>cap</td>
<td>caudal primary motoneuron</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>cns</td>
<td>central nervous system</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>den</td>
<td>dendrite</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>drg</td>
<td>dorsal root ganglia</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>dsn</td>
<td>dorsal spinal cord neuron</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>ect</td>
<td>ectoderm</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>end</td>
<td>endoderm</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>ens</td>
<td>enteric nervous system</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>ent</td>
<td>endothelium</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>epb</td>
<td>epiblast</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>epi</td>
<td>epidermis</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>epn</td>
<td>ependymal cells</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>eye</td>
<td>eye</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>fin</td>
<td>fin</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>fbr</td>
<td>forebrain</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>fnc</td>
<td>caudal fin</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>fnd</td>
<td>dorsal fin</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>fnl</td>
<td>pelvic fin</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>fnp</td>
<td>pectoral fin</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>fnv</td>
<td>ventral fin</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>gan</td>
<td>retinal ganglion neuron</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>ger</td>
<td>germ ring</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>gli</td>
<td>glia</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>har</td>
<td>hair cell</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>hat</td>
<td>hatching gland</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>hbr</td>
<td>hindbrain</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>hor</td>
<td>retinal horizontal cell</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>hrt</td>
<td>heart</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>hyb</td>
<td>hypoblast</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>int</td>
<td>interneuron</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>ipl</td>
<td>inner plexiform layer</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>kee</td>
<td>keel</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>lns</td>
<td>lens</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>mar</td>
<td>blastoderm margin</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>max</td>
<td>Mauthner axon</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>mbr</td>
<td>midbrain</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>mec</td>
<td>mesenchyme</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>mel</td>
<td>melanocyte</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>mes</td>
<td>mesoderm</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>mi1</td>
<td>middle hindbrain level 1</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>mi2</td>
<td>middle hindbrain level 2</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>mi3</td>
<td>middle hindbrain level 3</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>mid</td>
<td>mid neuron</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>mip</td>
<td>middle primary motoneuron</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>miv</td>
<td>miv neuron</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>mld</td>
<td>Mauthner lateral dendrite</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>mot</td>
<td>motoneuron</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>mps</td>
<td>muscle pioneers</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>mth</td>
<td>Mauthner neuron</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>mul</td>
<td>Muller cell in retina</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>mus</td>
<td>muscle</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>musb</td>
<td>branchial muscle</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>muse</td>
<td>enteric muscle</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>musep</td>
<td>epaxial muscle</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>mush</td>
<td>hypaxial muscle</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>musoc</td>
<td>extra-ocular muscle</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>mussf</td>
<td>muscle surface</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>mvd</td>
<td>Mauthner ventral dendrite</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>myc</td>
<td>myocomma cell</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>myo</td>
<td>myotome</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>ncr</td>
<td>neural crest cell</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>ner</td>
<td>nervous tissue</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>neu</td>
<td>neuron</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>nms</td>
<td>neuromast</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>npl</td>
<td>neuropil</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>nrt</td>
<td>neural retina</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>ntd</td>
<td>notochord</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>nts</td>
<td>notochord sheath</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>olf</td>
<td>olfactory pit/primordium</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>oln</td>
<td>nerve innervating olfactory bulb</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>opl</td>
<td>outer plexiform layer</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>otc</td>
<td>otocyst</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>oto</td>
<td>otic capsule</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>per</td>
<td>peridermis</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>phr</td>
<td>photoreceptor</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>pig</td>
<td>pigmented cell</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>pin</td>
<td>pineal gland</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>plg</td>
<td>posterior lateral line ganglion</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>pln</td>
<td>posterior lateral line nerve</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>plp</td>
<td>posterior lateral line primordium</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>pmn</td>
<td>primary motoneuron</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>pne</td>
<td>pronephritic kidney</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>pns</td>
<td>peripheral nervous system</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>prd</td>
<td>pronephritic duct</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>prt</td>
<td>pigmented retina</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>raf</td>
<td>radial fibers</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>rbn</td>
<td>Rohon-Beard neuron</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>ro1</td>
<td>rostral hindbrain level 1</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>ro2</td>
<td>rostral hindbrain level 2</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>ro3</td>
<td>rostral hindbrain level 3</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>rop</td>
<td>rostral primary motoneuron</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>rsn</td>
<td>reticulospinal neuron</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>shd</td>
<td>shield</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>smn</td>
<td>secondary motoneuron</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>snl</td>
<td>spinal cord lateral neuron</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>snp</td>
<td>spinal cord neuropil</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>snv</td>
<td>spinal cord ventral neuron</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>som</td>
<td>somite</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>spi</td>
<td>spinal cord</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>str</td>
<td>muscle striations</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>tin</td>
<td>t-interneuron</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>trg</td>
<td>trigeminal ganglion</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>tri</td>
<td>trigeminal nerve</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>vep</td>
<td>ventral ependymal plate cells</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>vsn</td>
<td>ventral spinal cord neuron</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>yol</td>
<td>yolk cell</td>
</tr>
<tr>
<td>&#160; &#160; &#160; &#160; &#160; &#160; &#160;</td>
<td>ysl</td>
<td>yolk syncytial layer</td>
</tr>
</table>
</div>
</zfin2:page>
